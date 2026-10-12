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
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;

/**
 * Hace el papel de la transportadora, del cliente y de la bodega. Revisa cada cierto tiempo la fecha
 * real: envía las órdenes pagadas, entrega las enviadas y sortea las devoluciones cuando se cumplen los plazos.
 */
@Service
@RequiredArgsConstructor
public class SimulationService {
    private static final Logger log = LoggerFactory.getLogger(SimulationService.class);

    static final String RETURN_REQUEST_REASON = "Automático: el cliente solicitó la devolución";
    static final String RETURN_REJECT_REASON = "Automático: el producto no cumple las condiciones de devolución";

    private final SimulationProperties properties;
    private final PurchaseOrderService orderService;
    private final PurchaseOrderRepository purchaseOrderRepository;
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

        // Read every list before changing anything, so an order moves at most one step per run
        List<PurchaseOrder> paid = ordersIn(OrderStatus.PAID);
        List<PurchaseOrder> shipped = ordersIn(OrderStatus.SHIPPED);
        List<PurchaseOrder> delivered = ordersIn(OrderStatus.DELIVERED);
        List<PurchaseOrder> returnRequested = ordersIn(OrderStatus.RETURN_REQUESTED);

        paid.forEach(order -> process(order, () -> {
            if (daysInStatus(order, today) >= properties.daysToShip()) {
                orderService.shipOrder(order.getId());
            }
        }));
        shipped.forEach(order -> process(order, () -> {
            if (daysInStatus(order, today) >= properties.daysToDeliver()) {
                orderService.deliverOrder(order.getId());
            }
        }));
        delivered.forEach(order -> process(order, () -> spinReturnRequest(order, today)));
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

    // Each order gets a single spin, so a rejected return is not requested again and again
    private void spinReturnRequest(PurchaseOrder order, LocalDate today) {
        if (order.isReturnSpinDone() || daysInStatus(order, today) < properties.daysToRequestReturn()) {
            return;
        }
        order.setReturnSpinDone(true);
        purchaseOrderRepository.save(order);

        if (random.nextDouble() < properties.returnRequestChance()) {
            orderService.returnOrder(order.getId(), RETURN_REQUEST_REASON);
        }
    }

    // Orders from before this feature have no status date: their last modification is the best guess
    private long daysInStatus(PurchaseOrder order, LocalDate today) {
        LocalDate since = order.getStatusChangedOn() != null
                ? order.getStatusChangedOn()
                : order.getUpdatedAt().toLocalDate();
        return ChronoUnit.DAYS.between(since, today);
    }

    private List<PurchaseOrder> ordersIn(OrderStatus status) {
        return purchaseOrderRepository.searchOrders(null, status);
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
