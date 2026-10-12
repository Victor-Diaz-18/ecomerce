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
    private Random random;

    private SimulationService simulationService;

    @BeforeEach
    void setUp() {
        // ship after 1 day, deliver after 3, return spin after 2 (20%), resolve after 3 (70% accepted)
        SimulationProperties properties = new SimulationProperties(true, 1, 3, 2, 0.2, 3, 0.7);
        Clock clock = Clock.fixed(TODAY.atTime(9, 0).atZone(ZONE).toInstant(), ZONE);
        simulationService = new SimulationService(properties, orderService, purchaseOrderRepository, clock, random);
    }

    private PurchaseOrder order(Long id, OrderStatus status, int daysAgo) {
        PurchaseOrder order = PurchaseOrder.builder()
                .id(id)
                .status(status)
                .statusChangedOn(TODAY.minusDays(daysAgo))
                .build();
        lenient().when(purchaseOrderRepository.searchOrders(null, status)).thenReturn(List.of(order));
        return order;
    }

    @Test
    void shouldShipPaidOrderOnceTheShippingDaysHavePassed() {
        order(1L, OrderStatus.PAID, 1);

        simulationService.advanceOrders();

        verify(orderService).shipOrder(1L);
    }

    @Test
    void shouldNotShipOrderPaidToday() {
        order(1L, OrderStatus.PAID, 0);

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
    void shouldUseLastUpdateForOrdersWithoutStatusDate() {
        PurchaseOrder legacy = order(1L, OrderStatus.PAID, 0);
        legacy.setStatusChangedOn(null);
        legacy.setUpdatedAt(LocalDateTime.of(2026, 9, 1, 10, 0));

        simulationService.advanceOrders();

        verify(orderService).shipOrder(1L);
    }

    @Test
    void shouldRequestReturnWhenTheRouletteSaysSo() {
        PurchaseOrder order = order(1L, OrderStatus.DELIVERED, 2);
        when(random.nextDouble()).thenReturn(0.1);

        simulationService.advanceOrders();

        assertTrue(order.isReturnSpinDone());
        verify(purchaseOrderRepository).save(order);
        verify(orderService).returnOrder(1L, SimulationService.RETURN_REQUEST_REASON);
    }

    @Test
    void shouldKeepOrderDeliveredWhenTheRouletteSaysNo() {
        PurchaseOrder order = order(1L, OrderStatus.DELIVERED, 2);
        when(random.nextDouble()).thenReturn(0.9);

        simulationService.advanceOrders();

        assertTrue(order.isReturnSpinDone());
        verifyNoInteractions(orderService);
    }

    @Test
    void shouldSpinTheReturnRouletteOnlyOncePerOrder() {
        PurchaseOrder order = order(1L, OrderStatus.DELIVERED, 10);
        order.setReturnSpinDone(true);

        simulationService.advanceOrders();

        verifyNoInteractions(random, orderService);
    }

    @Test
    void shouldNotSpinBeforeTheCustomerHasHadTheProductLongEnough() {
        PurchaseOrder order = order(1L, OrderStatus.DELIVERED, 1);

        simulationService.advanceOrders();

        assertFalse(order.isReturnSpinDone());
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
        when(purchaseOrderRepository.searchOrders(null, OrderStatus.PAID)).thenReturn(List.of(failing, fine));
        when(orderService.shipOrder(1L)).thenThrow(new BusinessException("boom"));

        simulationService.advanceOrders();

        verify(orderService).shipOrder(2L);
    }

    @Test
    void scheduledRunDoesNothingWhenDisabled() {
        SimulationProperties disabled = new SimulationProperties(false, 1, 3, 2, 0.2, 3, 0.7);
        SimulationService service = new SimulationService(disabled, orderService, purchaseOrderRepository,
                Clock.systemDefaultZone(), random);

        service.scheduledRun();

        verifyNoInteractions(purchaseOrderRepository, orderService);
        verify(orderService, never()).returnOrder(any(), anyString());
    }
}
