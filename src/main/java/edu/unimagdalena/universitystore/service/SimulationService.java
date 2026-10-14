package edu.unimagdalena.universitystore.service;

import edu.unimagdalena.universitystore.config.SimulationProperties;
import edu.unimagdalena.universitystore.entity.PurchaseOrder;
import edu.unimagdalena.universitystore.enums.OrderStatus;
import edu.unimagdalena.universitystore.repository.PurchaseOrderRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;

/**
 * Hace el papel de la transportadora y de la bodega. Revisa cada cierto tiempo la fecha real: envía las
 * órdenes pagadas, entrega las enviadas y resuelve las devoluciones cuando se cumplen los plazos.
 * Pedir una devolución es decisión del cliente, desde su portal.
 */
@Service
@RequiredArgsConstructor
public class SimulationService {
    private static final Logger log = LoggerFactory.getLogger(SimulationService.class);

    static final String RETURN_REJECT_REASON = "Automático: el producto no cumple las condiciones de devolución";

    private final SimulationProperties properties;
    private final PurchaseOrderService orderService;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final ShippingEstimator shippingEstimator;
    private final Clock clock;
    private final Random random;

    @Scheduled(fixedDelayString = "${simulation.check-interval-ms:60000}")
    public void scheduledRun() {
        if (properties.enabled()) {
            advanceOrders();
        }
    }

    public synchronized void advanceOrders() {
        LocalDate today = LocalDate.now(clock);
        boolean carrierWorksToday = shippingEstimator.isBusinessDay(today);

        // Read every list before changing anything, so an order moves at most one step per run
        List<PurchaseOrder> unpaid = ordersIn(OrderStatus.CREATED);
        List<PurchaseOrder> paid = ordersIn(OrderStatus.PAID);
        List<PurchaseOrder> shipped = ordersIn(OrderStatus.SHIPPED);
        List<PurchaseOrder> returnRequested = ordersIn(OrderStatus.RETURN_REQUESTED);

        LocalDateTime paymentDeadline = LocalDateTime.now(clock).minusHours(properties.unpaidOrderHours());
        unpaid.forEach(order -> process(order, () -> {
            if (order.getCreatedAt() != null && order.getCreatedAt().isBefore(paymentDeadline)) {
                orderService.cancelUnpaidOrder(order.getId());
            }
        }));
        paid.forEach(order -> process(order, () -> {
            if (carrierWorksToday && !today.isBefore(shipDate(order))) {
                orderService.shipOrder(order.getId());
            }
        }));
        shipped.forEach(order -> process(order, () -> {
            if (carrierWorksToday && !today.isBefore(arrivalDate(order))) {
                orderService.deliverOrder(order.getId());
            }
        }));
        returnRequested.forEach(order -> process(order, () -> {
            if (daysInStatus(order, today) >= properties.daysToResolveReturn()) {
                if (random.nextDouble() < properties.returnAcceptChance()) {
                    orderService.confirmReturn(order.getId());
                } else {
                    orderService.rejectReturn(order.getId(), RETURN_REJECT_REASON);
                }
            }
        }));
    }

    // Orders from before this feature have no status date: their last modification is the best guess
    private LocalDate statusDate(PurchaseOrder order) {
        return order.getStatusChangedOn() != null
                ? order.getStatusChangedOn()
                : order.getUpdatedAt().toLocalDate();
    }

    private long daysInStatus(PurchaseOrder order, LocalDate today) {
        return ChronoUnit.DAYS.between(statusDate(order), today);
    }

    // The real arrival was decided when the order shipped; older shipments fall back to the planned trip
    private LocalDate arrivalDate(PurchaseOrder order) {
        if (order.getArrivalDate() != null) {
            return order.getArrivalDate();
        }
        return shippingEstimator.estimatedDelivery(statusDate(order), order.getAddress());
    }

    // Orders paid before ship dates existed are ready to go
    private LocalDate shipDate(PurchaseOrder order) {
        return order.getShipDate() != null ? order.getShipDate() : statusDate(order);
    }

    private List<PurchaseOrder> ordersIn(OrderStatus status) {
        return purchaseOrderRepository.findByStatusWithAddress(status);
    }

    // One order failing (e.g. someone changed it at the same moment) must not stop the rest
    private void process(PurchaseOrder order, Runnable step) {
        try {
            step.run();
        } catch (RuntimeException e) {
            log.warn("No se pudo avanzar automáticamente la orden {}: {}", order.getId(), e.getMessage());
        }
    }
}
