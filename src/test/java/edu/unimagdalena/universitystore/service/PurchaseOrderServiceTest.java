package edu.unimagdalena.universitystore.service;

import edu.unimagdalena.universitystore.dto.OrderDtos.*;
import edu.unimagdalena.universitystore.enums.OrderStatus;
import edu.unimagdalena.universitystore.exception.BusinessException;
import edu.unimagdalena.universitystore.exception.ResourceNotFoundException;
import edu.unimagdalena.universitystore.mapper.OrderMapper;
import edu.unimagdalena.universitystore.repository.AddressRepository;
import edu.unimagdalena.universitystore.repository.CustomerRepository;
import edu.unimagdalena.universitystore.repository.InventoryRepository;
import edu.unimagdalena.universitystore.repository.OrderItemRepository;
import edu.unimagdalena.universitystore.repository.OrderStatusHistoryRepository;
import edu.unimagdalena.universitystore.repository.ProductPriceHistoryRepository;
import edu.unimagdalena.universitystore.repository.ProductRepository;
import edu.unimagdalena.universitystore.repository.PurchaseOrderRepository;
import edu.unimagdalena.universitystore.service.Impl.PurchaseOrderServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PurchaseOrderServiceImplTest {
    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private OrderStatusHistoryRepository orderStatusHistoryRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductPriceHistoryRepository priceHistoryRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private OrderMapper mapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private PurchaseOrderServiceImpl purchaseOrderService;

    @Test
    void shouldThrowExceptionWhenCreateOrderWithInvalidCustomer() {
        CreateOrderRequest request = new CreateOrderRequest(
                99L, 1L, List.of(new CreateOrderItemRequest(1L, 1))
        );

        when(customerRepository.existsByIdNotDeleted(99L)).thenReturn(false);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> purchaseOrderService.create(request));

        assertEquals("Customer not found", exception.getMessage());
    }

    @Test
    void shouldPayOrder() {
        var order = new edu.unimagdalena.universitystore.entity.PurchaseOrder();
        order.setId(1L);
        order.setStatus(OrderStatus.CREATED);

        when(purchaseOrderRepository.findById(1L))
                .thenReturn(Optional.of(order));
        when(purchaseOrderRepository.save(any()))
                .thenAnswer(i -> i.getArgument(0));
        when(mapper.toResponse(any())).thenAnswer(inv -> {
            var o = inv.getArgument(0, edu.unimagdalena.universitystore.entity.PurchaseOrder.class);
            return new OrderResponse(o.getId(), o.getStatus().name(), o.getCreatedAt(), 
                    o.getCustomer() != null ? o.getCustomer().getId() : null, "Customer", 
                    o.getAddress() != null ? o.getAddress().getId() : null, "Address", 
                    List.of(), o.getTotal());
        });

        OrderResponse result = purchaseOrderService.payOrder(1L);

        assertEquals(OrderStatus.PAID, OrderStatus.valueOf(result.status()));
    }

    @Test
    void shouldThrowExceptionWhenOrderNotFoundInPay() {
        when(purchaseOrderRepository.findById(1L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> purchaseOrderService.payOrder(1L));

        assertEquals("Order not found", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenOrderStatusInvalidForPay() {
        var order = new edu.unimagdalena.universitystore.entity.PurchaseOrder();
        order.setId(1L);
        order.setStatus(OrderStatus.PAID);

        when(purchaseOrderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> purchaseOrderService.payOrder(1L));

        assertEquals("Order must be in CREATED status to transition to PAID", exception.getMessage());
    }

    @Test
    void shouldFindAllOrders() {
        var order1 = new edu.unimagdalena.universitystore.entity.PurchaseOrder();
        order1.setStatus(OrderStatus.CREATED);
        var order2 = new edu.unimagdalena.universitystore.entity.PurchaseOrder();
        order2.setStatus(OrderStatus.PAID);

        when(purchaseOrderRepository.findAll())
                .thenReturn(List.of(order1, order2));
        when(mapper.toResponse(any())).thenAnswer(inv -> {
            var o = inv.getArgument(0, edu.unimagdalena.universitystore.entity.PurchaseOrder.class);
            return new OrderResponse(o.getId(), o.getStatus().name(), o.getCreatedAt(), 
                    o.getCustomer() != null ? o.getCustomer().getId() : null, "Customer", 
                    o.getAddress() != null ? o.getAddress().getId() : null, "Address", 
                    List.of(), o.getTotal());
        });

        List<OrderResponse> result = purchaseOrderService.findAll();

        assertEquals(2, result.size());
    }

    @Test
    void shouldFindOrderById() {
        var order = new edu.unimagdalena.universitystore.entity.PurchaseOrder();
        order.setId(1L);
        order.setStatus(OrderStatus.CREATED);

        when(purchaseOrderRepository.findById(1L))
                .thenReturn(Optional.of(order));
        when(mapper.toResponse(any())).thenAnswer(inv -> {
            var o = inv.getArgument(0, edu.unimagdalena.universitystore.entity.PurchaseOrder.class);
            return new OrderResponse(o.getId(), o.getStatus().name(), o.getCreatedAt(), 
                    o.getCustomer() != null ? o.getCustomer().getId() : null, "Customer", 
                    o.getAddress() != null ? o.getAddress().getId() : null, "Address", 
                    List.of(), o.getTotal());
        });

        OrderResponse result = purchaseOrderService.findById(1L);

        assertEquals(1L, result.id());
    }

    @Test
    void shouldThrowExceptionWhenFindOrderByIdFails() {
        when(purchaseOrderRepository.findById(1L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> purchaseOrderService.findById(1L));

        assertEquals("Order not found", exception.getMessage());
    }

    @Test
    void shouldCancelOrder() {
        var order = new edu.unimagdalena.universitystore.entity.PurchaseOrder();
        order.setId(1L);
        order.setStatus(OrderStatus.CREATED);

        when(purchaseOrderRepository.findById(1L))
                .thenReturn(Optional.of(order));
        when(purchaseOrderRepository.save(any()))
                .thenAnswer(i -> i.getArgument(0));
        when(mapper.toResponse(any())).thenAnswer(inv -> {
            var o = inv.getArgument(0, edu.unimagdalena.universitystore.entity.PurchaseOrder.class);
            return new OrderResponse(o.getId(), o.getStatus().name(), o.getCreatedAt(), 
                    o.getCustomer() != null ? o.getCustomer().getId() : null, "Customer", 
                    o.getAddress() != null ? o.getAddress().getId() : null, "Address", 
                    List.of(), o.getTotal());
        });

        OrderResponse result = purchaseOrderService.cancelOrder(1L);

        assertEquals(OrderStatus.CANCELLED, OrderStatus.valueOf(result.status()));
    }

    @Test
    void shouldThrowExceptionWhenCancelShippedOrder() {
        var order = new edu.unimagdalena.universitystore.entity.PurchaseOrder();
        order.setId(1L);
        order.setStatus(OrderStatus.SHIPPED);

        when(purchaseOrderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> purchaseOrderService.cancelOrder(1L));

        assertEquals("Shipped or delivered orders cannot be cancelled. Use return process instead.", exception.getMessage());
    }
}