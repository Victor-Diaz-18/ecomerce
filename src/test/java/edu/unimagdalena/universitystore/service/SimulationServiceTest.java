package edu.unimagdalena.universitystore.service;

import edu.unimagdalena.universitystore.config.SimulationProperties;
import edu.unimagdalena.universitystore.entity.PurchaseOrder;
import edu.unimagdalena.universitystore.enums.OrderStatus;
import edu.unimagdalena.universitystore.exception.BusinessException;
import edu.unimagdalena.universitystore.repository.PurchaseOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SimulationServiceTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 12);
    private static final ZoneId ZONE = ZoneId.of("America/Bogota");

    @Mock
    private PurchaseOrderService orderService;

    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;

    @Mock
    private ShippingEstimator shippingEstimator;

    @Mock
    private Random random;

    private SimulationService simulationService;

    @BeforeEach
    void setUp() {
        // returns resolved after 3 days (70% accepted)
        SimulationProperties properties = properties(true);
        lenient().when(shippingEstimator.isBusinessDay(any())).thenReturn(true);
        lenient().when(shippingEstimator.estimatedDelivery(any(), any()))
                .thenAnswer(inv -> inv.getArgument(0, LocalDate.class).plusDays(3));
        Clock clock = Clock.fixed(TODAY.atTime(9, 0).atZone(ZONE).toInstant(), ZONE);
        simulationService = new SimulationService(properties, orderService, purchaseOrderRepository, shippingEstimator, clock, random);
    }

    private static SimulationProperties properties(boolean enabled) {
        return new SimulationProperties(enabled, "Santa Marta", "Colombia", 14, 300, 1, 2, 3, 6, 0.2, 0.2, 3, 0.7);
    }

    private PurchaseOrder order(Long id, OrderStatus status, int daysAgo) {
        PurchaseOrder order = PurchaseOrder.builder()
                .id(id)
                .status(status)
                .statusChangedOn(TODAY.minusDays(daysAgo))
                .build();
        lenient().when(purchaseOrderRepository.findByStatusWithAddress(status)).thenReturn(List.of(order));
        return order;
    }

    @Test
    void shouldShipPaidOrderWhoseShipDateHasPassed() {
        order(1L, OrderStatus.PAID, 1).setShipDate(TODAY.minusDays(1));

        simulationService.advanceOrders();

        verify(orderService).shipOrder(1L);
    }

    @Test
    void shouldShipTheSameDayWhenPaidBeforeTheCutOff() {
        order(1L, OrderStatus.PAID, 0).setShipDate(TODAY);

        simulationService.advanceOrders();

        verify(orderService).shipOrder(1L);
    }

    @Test
    void shouldWaitForTheShipDateWhenPaidAfterTheCutOff() {
        order(1L, OrderStatus.PAID, 0).setShipDate(TODAY.plusDays(1));

        simulationService.advanceOrders();

        verifyNoInteractions(orderService);
    }

    @Test
    void nothingShipsOrArrivesWhenTheCarrierIsNotWorking() {
        order(1L, OrderStatus.PAID, 3).setShipDate(TODAY.minusDays(3));
        order(2L, OrderStatus.SHIPPED, 9).setArrivalDate(TODAY.minusDays(1));
        when(shippingEstimator.isBusinessDay(TODAY)).thenReturn(false);

        simulationService.advanceOrders();

        verifyNoInteractions(orderService);
    }

    @Test
    void shouldDeliverShippedOrderOnlyAfterDeliveryDays() {
        order(1L, OrderStatus.SHIPPED, 2);
        simulationService.advanceOrders();
        verify(orderService, never()).deliverOrder(anyLong());

        order(1L, OrderStatus.SHIPPED, 3);
        simulationService.advanceOrders();
        verify(orderService).deliverOrder(1L);
    }

    @Test
    void shouldDeliverOnTheRealArrivalDateEvenIfItDiffersFromTheEstimate() {
        PurchaseOrder order = order(1L, OrderStatus.SHIPPED, 5);
        order.setEstimatedDelivery(TODAY.minusDays(1));
        order.setArrivalDate(TODAY.plusDays(1));
        simulationService.advanceOrders();
        verify(orderService, never()).deliverOrder(anyLong());

        order.setArrivalDate(TODAY);
        simulationService.advanceOrders();
        verify(orderService).deliverOrder(1L);
    }

    @Test
    void shouldUseLastUpdateForOrdersWithoutStatusDate() {
        PurchaseOrder legacy = order(1L, OrderStatus.PAID, 0);
        legacy.setStatusChangedOn(null);
        legacy.setUpdatedAt(LocalDateTime.of(2026, 9, 1, 10, 0));

        simulationService.advanceOrders();

        verify(orderService).shipOrder(1L);
    }

    @Test
    void deliveredOrdersStayDeliveredUntilTheCustomerAsksForAReturn() {
        order(1L, OrderStatus.DELIVERED, 30);

        simulationService.advanceOrders();

        verifyNoInteractions(random, orderService);
    }

    @Test
    void shouldAcceptOrRejectPendingReturnsByRoulette() {
        order(1L, OrderStatus.RETURN_REQUESTED, 3);

        when(random.nextDouble()).thenReturn(0.5);
        simulationService.advanceOrders();
        verify(orderService).confirmReturn(1L);

        when(random.nextDouble()).thenReturn(0.95);
        simulationService.advanceOrders();
        verify(orderService).rejectReturn(1L, SimulationService.RETURN_REJECT_REASON);
    }

    @Test
    void shouldLeaveRecentReturnRequestsPending() {
        order(1L, OrderStatus.RETURN_REQUESTED, 2);

        simulationService.advanceOrders();

        verifyNoInteractions(random, orderService);
    }

    @Test
    void oneFailingOrderShouldNotStopTheOthers() {
        PurchaseOrder failing = PurchaseOrder.builder().id(1L).status(OrderStatus.PAID)
                .statusChangedOn(TODAY.minusDays(5)).build();
        PurchaseOrder fine = PurchaseOrder.builder().id(2L).status(OrderStatus.PAID)
                .statusChangedOn(TODAY.minusDays(5)).build();
        when(purchaseOrderRepository.findByStatusWithAddress(OrderStatus.PAID)).thenReturn(List.of(failing, fine));
        when(orderService.shipOrder(1L)).thenThrow(new BusinessException("boom"));

        simulationService.advanceOrders();

        verify(orderService).shipOrder(2L);
    }

    @Test
    void scheduledRunDoesNothingWhenDisabled() {
        SimulationService service = new SimulationService(properties(false), orderService, purchaseOrderRepository,
                shippingEstimator, Clock.systemDefaultZone(), random);

        service.scheduledRun();

        verifyNoInteractions(purchaseOrderRepository, orderService);
        verify(orderService, never()).returnOrder(any(), anyString());
    }
}
