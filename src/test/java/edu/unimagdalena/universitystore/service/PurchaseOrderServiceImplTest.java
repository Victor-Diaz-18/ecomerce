package edu.unimagdalena.universitystore.service;

import edu.unimagdalena.universitystore.entity.PurchaseOrder;
import edu.unimagdalena.universitystore.enums.OrderStatus;
import edu.unimagdalena.universitystore.repository.InventoryRepository;
import edu.unimagdalena.universitystore.repository.PurchaseOrderRepository;
import edu.unimagdalena.universitystore.service.Impl.PurchaseOrderServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PurchaseOrderServiceImplTest {
    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private PurchaseOrderServiceImpl purchaseOrderService;

    @Test
    void shouldCreateOrder() {
        PurchaseOrder order = PurchaseOrder.builder()
                .total(BigDecimal.valueOf(100000))
                .build();

        when(purchaseOrderRepository.save(any(PurchaseOrder.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PurchaseOrder result = purchaseOrderService.create(order);

        assertNotNull(result.getCreatedAt());
        assertEquals(OrderStatus.CREATED, result.getStatus());
        assertEquals(BigDecimal.valueOf(100000), result.getTotal());
    }

    @Test
    void shouldPayOrder() {
        PurchaseOrder order = PurchaseOrder.builder()
                .id(1L)
                .status(OrderStatus.CREATED)
                .build();

        when(purchaseOrderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(purchaseOrderRepository.save(any(PurchaseOrder.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PurchaseOrder result = purchaseOrderService.payOrder(1L);

        assertEquals(OrderStatus.PAID, result.getStatus());
    }

    @Test
    void shouldThrowExceptionWhenOrderNotFoundInPay() {
        when(purchaseOrderRepository.findById(1L))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> purchaseOrderService.payOrder(1L));

        assertEquals("Order not found", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenOrderStatusInvalidForPay() {
        PurchaseOrder order = PurchaseOrder.builder()
                .id(1L)
                .status(OrderStatus.PAID)
                .build();

        when(purchaseOrderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> purchaseOrderService.payOrder(1L));

        assertEquals("Only CREATED orders can be paid", exception.getMessage());
    }

    @Test
    void shouldFindAllOrders() {
        when(purchaseOrderRepository.findAll())
                .thenReturn(List.of(new PurchaseOrder(), new PurchaseOrder()));

        List<PurchaseOrder> result = purchaseOrderService.findAll();

        assertEquals(2, result.size());
    }

    @Test
    void shouldFindOrderById() {
        PurchaseOrder order = PurchaseOrder.builder()
                .id(1L)
                .build();

        when(purchaseOrderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        PurchaseOrder result = purchaseOrderService.findById(1L);

        assertEquals(1L, result.getId());
    }

    @Test
    void shouldThrowExceptionWhenFindOrderByIdFails() {
        when(purchaseOrderRepository.findById(1L))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> purchaseOrderService.findById(1L));

        assertEquals("Order not found", exception.getMessage());
    }

    @Test
    void shouldCancelOrder() {
        PurchaseOrder order = PurchaseOrder.builder()
                .id(1L)
                .status(OrderStatus.CREATED)
                .build();

        when(purchaseOrderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(purchaseOrderRepository.save(any(PurchaseOrder.class)))
                .thenAnswer(i -> i.getArgument(0));

        PurchaseOrder result = purchaseOrderService.cancelOrder(1L);

        assertEquals(OrderStatus.CANCELLED, result.getStatus());
    }

    @Test
    void shouldThrowExceptionWhenCancelPaidOrder() {
        PurchaseOrder order = PurchaseOrder.builder()
                .id(1L)
                .status(OrderStatus.PAID)
                .build();

        when(purchaseOrderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> purchaseOrderService.cancelOrder(1L));

        assertEquals("Paid orders cannot be cancelled", exception.getMessage());
    }
}