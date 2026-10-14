package edu.unimagdalena.universitystore.service;

import edu.unimagdalena.universitystore.config.StorePolicy;
import edu.unimagdalena.universitystore.dto.OrderDtos.*;
import edu.unimagdalena.universitystore.entity.Address;
import edu.unimagdalena.universitystore.entity.Customer;
import edu.unimagdalena.universitystore.entity.Inventory;
import edu.unimagdalena.universitystore.entity.OrderItem;
import edu.unimagdalena.universitystore.entity.Product;
import edu.unimagdalena.universitystore.entity.PurchaseOrder;
import edu.unimagdalena.universitystore.enums.OrderStatus;
import edu.unimagdalena.universitystore.exception.BusinessException;
import edu.unimagdalena.universitystore.exception.ResourceNotFoundException;
import edu.unimagdalena.universitystore.exception.ValidationException;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDate;
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
    private ShippingEstimator shippingEstimator;

    @Mock
    private StorePolicy storePolicy;

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

    @BeforeEach
    void setUp() {
        lenient().when(shippingEstimator.deliversTo(any())).thenReturn(true);
    }

    @Test
    void shouldNotCreateOrderForAddressAbroad() {
        when(customerRepository.existsByIdNotDeleted(1L)).thenReturn(true);
        when(addressRepository.existsByIdNotDeleted(1L)).thenReturn(true);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(new Customer()));
        when(addressRepository.findById(1L)).thenReturn(Optional.of(new Address()));
        when(shippingEstimator.deliversTo(any())).thenReturn(false);
        CreateOrderRequest request = new CreateOrderRequest(1L, 1L, List.of(new CreateOrderItemRequest(5L, 1)));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> purchaseOrderService.create(request));

        assertEquals("Orders can only be shipped to addresses in Colombia", exception.getMessage());
        verifyNoInteractions(inventoryRepository);
    }

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
                    List.of(), o.getTotal(), o.getEstimatedDelivery(), o.getDeliveredOn());
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
                    List.of(), o.getTotal(), o.getEstimatedDelivery(), o.getDeliveredOn());
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
                    List.of(), o.getTotal(), o.getEstimatedDelivery(), o.getDeliveredOn());
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
                    List.of(), o.getTotal(), o.getEstimatedDelivery(), o.getDeliveredOn());
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

        assertEquals("Paid, shipped or delivered orders cannot be cancelled. Use return process instead.", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenCancelPaidOrder() {
        var order = new edu.unimagdalena.universitystore.entity.PurchaseOrder();
        order.setId(1L);
        order.setStatus(OrderStatus.PAID);

        when(purchaseOrderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> purchaseOrderService.cancelOrder(1L));

        assertEquals("Paid, shipped or delivered orders cannot be cancelled. Use return process instead.", exception.getMessage());
        verify(purchaseOrderRepository, never()).save(any());
    }

    @Test
    void shouldReturnPaidOrder() {
        var order = new edu.unimagdalena.universitystore.entity.PurchaseOrder();
        order.setId(1L);
        order.setStatus(OrderStatus.PAID);

        when(purchaseOrderRepository.findById(1L))
                .thenReturn(Optional.of(order));
        when(purchaseOrderRepository.save(any()))
                .thenAnswer(i -> i.getArgument(0));
        when(mapper.toResponse(any())).thenAnswer(inv -> {
            var o = inv.getArgument(0, edu.unimagdalena.universitystore.entity.PurchaseOrder.class);
            return new OrderResponse(o.getId(), o.getStatus().name(), o.getCreatedAt(),
                    null, "Customer", null, "Address", List.of(), o.getTotal(), o.getEstimatedDelivery(), o.getDeliveredOn());
        });

        OrderResponse result = purchaseOrderService.returnOrder(1L, "Cliente se arrepintio");

        assertEquals(OrderStatus.RETURNED, OrderStatus.valueOf(result.status()));
    }

    @Test
    void shouldThrowExceptionWhenReturnCreatedOrder() {
        var order = new edu.unimagdalena.universitystore.entity.PurchaseOrder();
        order.setId(1L);
        order.setStatus(OrderStatus.CREATED);

        when(purchaseOrderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> purchaseOrderService.returnOrder(1L, null));

        assertEquals("Only paid or delivered orders can be returned", exception.getMessage());
    }

    private PurchaseOrder orderWithStatus(OrderStatus status) {
        PurchaseOrder order = new PurchaseOrder();
        order.setId(1L);
        order.setStatus(status);
        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(order));
        return order;
    }

    private void stubSaveAndMapper() {
        when(purchaseOrderRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(mapper.toResponse(any())).thenAnswer(inv -> {
            PurchaseOrder o = inv.getArgument(0, PurchaseOrder.class);
            return new OrderResponse(o.getId(), o.getStatus().name(), o.getCreatedAt(),
                    null, "Customer", null, "Address", List.of(), o.getTotal(), o.getEstimatedDelivery(), o.getDeliveredOn());
        });
    }

    private Inventory stubOneItemInStock(int availableStock, int quantity) {
        Product product = Product.builder().id(5L).build();
        Inventory inventory = Inventory.builder().availableStock(availableStock).product(product).build();
        when(orderItemRepository.findByOrderId(1L))
                .thenReturn(List.of(OrderItem.builder().product(product).quantity(quantity).build()));
        when(inventoryRepository.findByProductIdForUpdate(5L)).thenReturn(Optional.of(inventory));
        return inventory;
    }

    @Test
    void returningDeliveredOrderOnlyRequestsReturnAndKeepsStock() {
        orderWithStatus(OrderStatus.DELIVERED);
        stubSaveAndMapper();

        OrderResponse result = purchaseOrderService.returnOrder(1L, "Llego rota");

        assertEquals("RETURN_REQUESTED", result.status());
        verifyNoInteractions(inventoryRepository);
    }

    @Test
    void shouldNotReturnOrderThatIsStillOnItsWay() {
        PurchaseOrder order = orderWithStatus(OrderStatus.SHIPPED);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> purchaseOrderService.returnOrder(1L, null));

        assertEquals("Shipped orders cannot be returned until they are delivered", exception.getMessage());
        assertEquals(OrderStatus.SHIPPED, order.getStatus());
        verifyNoInteractions(inventoryRepository);
    }

    @Test
    void returningPaidOrderRefundsAndRestoresStockImmediately() {
        orderWithStatus(OrderStatus.PAID);
        stubSaveAndMapper();
        Inventory inventory = stubOneItemInStock(10, 2);

        OrderResponse result = purchaseOrderService.returnOrder(1L, null);

        assertEquals("RETURNED", result.status());
        assertEquals(12, inventory.getAvailableStock());
    }

    @Test
    void estimateIsSetAtPaymentAndRealDeliveryDateIsKeptSeparately() {
        PurchaseOrder order = orderWithStatus(OrderStatus.CREATED);
        stubSaveAndMapper();
        LocalDate today = LocalDate.now();
        when(shippingEstimator.shipDate(any())).thenReturn(today.plusDays(1));
        when(shippingEstimator.estimatedDelivery(eq(today.plusDays(1)), any())).thenReturn(today.plusDays(4));
        when(shippingEstimator.actualArrival(eq(today), any())).thenReturn(today.plusDays(5));

        assertEquals(today.plusDays(4), purchaseOrderService.payOrder(1L).estimatedDelivery());
        assertEquals(today.plusDays(1), order.getShipDate());

        purchaseOrderService.shipOrder(1L);
        assertEquals(today.plusDays(5), order.getArrivalDate());

        OrderResponse delivered = purchaseOrderService.deliverOrder(1L);
        assertEquals(today.plusDays(4), delivered.estimatedDelivery());
        assertEquals(today, delivered.deliveredOn());
    }

    private Address addressOf(Long id, Long customerId, String city) {
        return Address.builder().id(id).street("Calle 1 # 2-3").city(city).country("Colombia")
                .customer(Customer.builder().id(customerId).build()).build();
    }

    @Test
    void shippingFreezesTheDeliveryAddress() {
        PurchaseOrder order = orderWithStatus(OrderStatus.PAID);
        Address address = addressOf(7L, 1L, "Santa Marta");
        order.setAddress(address);
        stubSaveAndMapper();

        purchaseOrderService.shipOrder(1L);
        address.setCity("Bogotá");

        assertEquals("Calle 1 # 2-3, Santa Marta, Colombia", order.getShippingAddress());
    }

    @Test
    void paidOrderCanMoveToAnotherAddressOfTheSameCustomer() {
        PurchaseOrder order = orderWithStatus(OrderStatus.PAID);
        order.setCustomer(Customer.builder().id(1L).build());
        order.setShipDate(LocalDate.of(2026, 10, 13));
        Address bogota = addressOf(8L, 1L, "Bogotá");
        when(addressRepository.findById(8L)).thenReturn(Optional.of(bogota));
        when(shippingEstimator.estimatedDelivery(LocalDate.of(2026, 10, 13), bogota)).thenReturn(LocalDate.of(2026, 10, 16));
        stubSaveAndMapper();

        OrderResponse result = purchaseOrderService.changeAddress(1L, 8L);

        assertSame(bogota, order.getAddress());
        assertEquals(LocalDate.of(2026, 10, 16), result.estimatedDelivery());
    }

    @Test
    void shippedOrderCannotChangeAddress() {
        orderWithStatus(OrderStatus.SHIPPED);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> purchaseOrderService.changeAddress(1L, 8L));

        assertEquals("The delivery address can only be changed before the order is shipped", exception.getMessage());
        verifyNoInteractions(addressRepository);
    }

    @Test
    void orderCannotUseAnAddressOfAnotherCustomer() {
        PurchaseOrder order = orderWithStatus(OrderStatus.CREATED);
        order.setCustomer(Customer.builder().id(1L).build());
        when(addressRepository.findById(8L)).thenReturn(Optional.of(addressOf(8L, 2L, "Bogotá")));

        assertThrows(ValidationException.class, () -> purchaseOrderService.changeAddress(1L, 8L));
    }

    @Test
    void editingAnAddressRefreshesTheEstimateOfPaidOrders() {
        PurchaseOrder order = PurchaseOrder.builder().id(1L).status(OrderStatus.PAID)
                .shipDate(LocalDate.of(2026, 10, 13)).address(addressOf(8L, 1L, "Bogotá")).build();
        when(purchaseOrderRepository.findByAddressIdAndStatus(8L, OrderStatus.PAID)).thenReturn(List.of(order));
        when(shippingEstimator.estimatedDelivery(eq(LocalDate.of(2026, 10, 13)), any())).thenReturn(LocalDate.of(2026, 10, 16));

        purchaseOrderService.refreshEstimatedDelivery(8L);

        assertEquals(LocalDate.of(2026, 10, 16), order.getEstimatedDelivery());
        verify(purchaseOrderRepository).save(order);
    }

    @Test
    void unpaidOrderCancelledByTheDeadlineGivesItsUnitsBackAndSaysWhy() {
        orderWithStatus(OrderStatus.CREATED);
        stubSaveAndMapper();
        Inventory inventory = stubOneItemInStock(10, 2);

        assertEquals("CANCELLED", purchaseOrderService.cancelUnpaidOrder(1L).status());
        assertEquals(12, inventory.getAvailableStock());
        verify(orderStatusHistoryRepository).save(argThat(h ->
                PurchaseOrderServiceImpl.UNPAID_CANCEL_REASON.equals(h.getReason())));
    }

    @Test
    void deliveredOrderCanBeReturnedWithinThePolicyDays() {
        PurchaseOrder order = orderWithStatus(OrderStatus.DELIVERED);
        order.setDeliveredOn(LocalDate.now().minusDays(30));
        when(storePolicy.returnDays()).thenReturn(30);
        stubSaveAndMapper();

        assertEquals("RETURN_REQUESTED", purchaseOrderService.returnOrder(1L, "No me quedó").status());
    }

    @Test
    void deliveredOrderCannotBeReturnedAfterThePolicyDays() {
        PurchaseOrder order = orderWithStatus(OrderStatus.DELIVERED);
        order.setDeliveredOn(LocalDate.now().minusDays(31));
        when(storePolicy.returnDays()).thenReturn(30);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> purchaseOrderService.returnOrder(1L, null));

        assertTrue(exception.getMessage().startsWith("El plazo para devolver esta compra terminó el"));
        assertEquals(OrderStatus.DELIVERED, order.getStatus());
    }

    @Test
    void shouldRejectSecondReturnRequest() {
        orderWithStatus(OrderStatus.RETURN_REQUESTED);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> purchaseOrderService.returnOrder(1L, null));

        assertEquals("A return has already been requested for this order", exception.getMessage());
    }

    @Test
    void confirmingReturnRestoresStockAndMarksReturned() {
        orderWithStatus(OrderStatus.RETURN_REQUESTED);
        stubSaveAndMapper();
        Inventory inventory = stubOneItemInStock(10, 3);

        OrderResponse result = purchaseOrderService.confirmReturn(1L);

        assertEquals("RETURNED", result.status());
        assertEquals(13, inventory.getAvailableStock());
    }

    @Test
    void shouldNotConfirmReturnThatWasNeverRequested() {
        orderWithStatus(OrderStatus.DELIVERED);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> purchaseOrderService.confirmReturn(1L));

        assertEquals("Order has no pending return request", exception.getMessage());
        verifyNoInteractions(inventoryRepository);
    }

    @Test
    void rejectingReturnPutsOrderBackToDelivered() {
        orderWithStatus(OrderStatus.RETURN_REQUESTED);
        stubSaveAndMapper();

        OrderResponse result = purchaseOrderService.rejectReturn(1L, "El paquete nunca llego");

        assertEquals("DELIVERED", result.status());
        verifyNoInteractions(inventoryRepository);
    }

    @Test
    void shouldMapMonthlyIncomeRows() {
        when(purchaseOrderRepository.findMonthlyIncome()).thenReturn(List.<Object[]>of(
                new Object[]{2026, 6, new BigDecimal("1500.00")}
        ));

        assertEquals(List.of(new MonthlyIncomeResponse(2026, 6, new BigDecimal("1500.00"))),
                purchaseOrderService.getMonthlyIncome());
    }

    @Test
    void shouldMapBestSellingProductRows() {
        when(purchaseOrderRepository.findBestSellingProducts()).thenReturn(List.<Object[]>of(
                new Object[]{"Camisa", 5L},
                new Object[]{"Gorra", 1L}
        ));

        assertEquals(List.of(
                new BestSellingProductResponse("Camisa", 5L),
                new BestSellingProductResponse("Gorra", 1L)
        ), purchaseOrderService.getBestSellingProducts());
    }

    @Test
    void shouldMapTopCustomerRows() {
        when(purchaseOrderRepository.findTopCustomers()).thenReturn(List.<Object[]>of(
                new Object[]{"Ana", 2L}
        ));

        assertEquals(List.of(new TopCustomerResponse("Ana", 2L)), purchaseOrderService.getTopCustomers());
    }

    @Test
    void shouldMapIncomeByStatusRowsUsingEnumName() {
        when(purchaseOrderRepository.findIncomeByStatus()).thenReturn(List.<Object[]>of(
                new Object[]{OrderStatus.CANCELLED, new BigDecimal("500")}
        ));

        assertEquals(List.of(new IncomeByStatusResponse("CANCELLED", new BigDecimal("500"))),
                purchaseOrderService.getIncomeByStatus());
    }
}