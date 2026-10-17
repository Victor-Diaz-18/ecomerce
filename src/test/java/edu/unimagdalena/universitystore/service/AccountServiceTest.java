package edu.unimagdalena.universitystore.service;

import edu.unimagdalena.universitystore.dto.AccountDtos.BuyNowRequest;
import edu.unimagdalena.universitystore.dto.AccountDtos.CreateMyOrderRequest;
import edu.unimagdalena.universitystore.dto.OrderDtos.CreateOrderItemRequest;
import edu.unimagdalena.universitystore.dto.OrderDtos.CreateOrderRequest;
import edu.unimagdalena.universitystore.dto.OrderDtos.OrderResponse;
import edu.unimagdalena.universitystore.entity.Address;
import edu.unimagdalena.universitystore.entity.Customer;
import edu.unimagdalena.universitystore.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
    private static final Long ME = 7L;
    private static final Long SOMEONE_ELSE = 8L;

    @Mock
    private CustomerService customerService;
    @Mock
    private AddressService addressService;
    @Mock
    private PurchaseOrderService orderService;

    @InjectMocks
    private AccountService accountService;

    private void orderBelongsTo(Long orderId, Long customerId) {
        when(orderService.findById(orderId)).thenReturn(new OrderResponse(orderId, "CREATED", null, customerId,
                "Cliente", 1L, "Calle 1", List.of(), BigDecimal.TEN, null, null));
    }

    private void addressBelongsTo(Long addressId, Long customerId) {
        when(addressService.findById(addressId)).thenReturn(Address.builder().id(addressId)
                .customer(Customer.builder().id(customerId).build()).build());
    }

    @Test
    void profileChangeOnlyTouchesTheName() {
        accountService.updateName(ME, "  Ana María  ");

        ArgumentCaptor<Customer> changes = ArgumentCaptor.forClass(Customer.class);
        verify(customerService).update(eq(ME), changes.capture());
        assertEquals("Ana María", changes.getValue().getName());
        assertNull(changes.getValue().getEmail());
        assertNull(changes.getValue().getStatus());
    }

    @Test
    void listsOnlyTheOrdersOfTheCustomer() {
        accountService.orders(ME);

        verify(orderService).search(ME, null);
    }

    @Test
    void newAddressIsAlwaysCreatedForTheCustomerOfTheSession() {
        accountService.addAddress(ME, "Calle 22 # 4-15", "Centro", "Santa Marta");

        ArgumentCaptor<Address> created = ArgumentCaptor.forClass(Address.class);
        verify(addressService).create(created.capture());
        assertEquals(ME, created.getValue().getCustomer().getId());
        assertEquals("Centro", created.getValue().getNeighborhood());
    }

    @Test
    void createsTheOrderForTheCustomerOfTheSession() {
        addressBelongsTo(3L, ME);
        List<CreateOrderItemRequest> items = List.of(new CreateOrderItemRequest(5L, 2));

        accountService.createOrder(ME, new CreateMyOrderRequest(3L, items));

        verify(orderService).create(new CreateOrderRequest(ME, 3L, items));
    }

    @Test
    void buyNowCreatesAndPaysASingleProductOrder() {
        addressBelongsTo(3L, ME);
        when(orderService.create(any())).thenReturn(new OrderResponse(40L, "CREATED", null, ME,
                "Cliente", 3L, "Calle 1", List.of(), BigDecimal.TEN, null, null));

        accountService.buyNow(ME, new BuyNowRequest(3L, 5L, 2));

        verify(orderService).create(new CreateOrderRequest(ME, 3L, List.of(new CreateOrderItemRequest(5L, 2))));
        verify(orderService).payOrder(40L);
    }

    @Test
    void whenThePaymentFailsTheOrderStaysPendingForAnotherTry() {
        addressBelongsTo(3L, ME);
        OrderResponse pending = new OrderResponse(40L, "CREATED", null, ME,
                "Cliente", 3L, "Calle 1", List.of(), BigDecimal.TEN, null, null);
        when(orderService.create(any())).thenReturn(pending);
        when(orderService.payOrder(40L)).thenThrow(new RuntimeException("Pago rechazado"));
        when(orderService.findById(40L)).thenReturn(pending);

        OrderResponse result = accountService.buyNow(ME, new BuyNowRequest(3L, 5L, 1));

        assertEquals("CREATED", result.status());
        verify(orderService, never()).cancelOrder(any());
    }

    @Test
    void buyNowRejectsAnAddressOfAnotherCustomer() {
        addressBelongsTo(3L, SOMEONE_ELSE);

        assertThrows(ResourceNotFoundException.class, () -> accountService.buyNow(ME, new BuyNowRequest(3L, 5L, 1)));

        verify(orderService, never()).create(any());
        verify(orderService, never()).payOrder(any());
    }

    @Test
    void cannotOrderToAnAddressOfAnotherCustomer() {
        addressBelongsTo(3L, SOMEONE_ELSE);

        assertThrows(ResourceNotFoundException.class, () -> accountService.createOrder(ME,
                new CreateMyOrderRequest(3L, List.of(new CreateOrderItemRequest(5L, 2)))));

        verify(orderService, never()).create(any());
    }

    @Test
    void canPayCancelAndReturnItsOwnOrder() {
        orderBelongsTo(15L, ME);

        accountService.payOrder(ME, 15L);
        accountService.cancelOrder(ME, 15L);
        accountService.returnOrder(ME, 15L, "No me sirvió");
        accountService.changeOrderAddress(ME, 15L, 3L);

        verify(orderService).payOrder(15L);
        verify(orderService).cancelOrder(15L);
        verify(orderService).returnOrder(15L, "No me sirvió");
        verify(orderService).changeAddress(15L, 3L);
    }

    @Test
    void anotherCustomersOrderLooksAsIfItDidNotExist() {
        orderBelongsTo(15L, SOMEONE_ELSE);

        assertThrows(ResourceNotFoundException.class, () -> accountService.order(ME, 15L));
        assertThrows(ResourceNotFoundException.class, () -> accountService.payOrder(ME, 15L));
        assertThrows(ResourceNotFoundException.class, () -> accountService.cancelOrder(ME, 15L));
        assertThrows(ResourceNotFoundException.class, () -> accountService.returnOrder(ME, 15L, null));
        assertThrows(ResourceNotFoundException.class, () -> accountService.changeOrderAddress(ME, 15L, 3L));

        verify(orderService, never()).payOrder(any());
        verify(orderService, never()).cancelOrder(any());
        verify(orderService, never()).returnOrder(any(), any());
        verify(orderService, never()).changeAddress(any(), any());
    }

    @Test
    void historyOnlyForTheCustomersOwnOrder() {
        orderBelongsTo(15L, ME);
        accountService.orderHistory(ME, 15L);
        verify(orderService).getHistory(15L);

        orderBelongsTo(16L, SOMEONE_ELSE);
        assertThrows(ResourceNotFoundException.class, () -> accountService.orderHistory(ME, 16L));
        verify(orderService, never()).getHistory(16L);
    }

    @Test
    void cannotEditAnAddressOfAnotherCustomer() {
        addressBelongsTo(3L, SOMEONE_ELSE);

        assertThrows(ResourceNotFoundException.class,
                () -> accountService.updateAddress(ME, 3L, "Calle 1 # 1-1", "Centro", "Bogotá"));

        verify(addressService, never()).update(any(), any(), any(), any());
    }
}
