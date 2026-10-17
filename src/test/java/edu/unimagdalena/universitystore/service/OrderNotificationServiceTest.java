package edu.unimagdalena.universitystore.service;

import edu.unimagdalena.universitystore.dto.OrderDtos.OrderItemResponse;
import edu.unimagdalena.universitystore.dto.OrderDtos.OrderResponse;
import edu.unimagdalena.universitystore.entity.Customer;
import edu.unimagdalena.universitystore.enums.OrderStatus;
import edu.unimagdalena.universitystore.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderNotificationServiceTest {
    @Mock
    private PurchaseOrderService orderService;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private OrderNotificationService service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "storeUrl", "https://tienda.test");
        OrderResponse order = new OrderResponse(15L, "PAID", null, 7L, "Ana", 3L, "Calle 1 # 2-3, Santa Marta",
                List.of(new OrderItemResponse(1L, 5L, "Camisa", 2, new BigDecimal("20000"), new BigDecimal("40000"))),
                new BigDecimal("40000"), LocalDate.of(2026, 10, 20), null);
        lenient().when(orderService.findById(15L)).thenReturn(order);
        lenient().when(customerRepository.findById(7L))
                .thenReturn(Optional.of(Customer.builder().id(7L).name("Ana").email("ana@correo.com").build()));
    }

    @Test
    void paymentEmailTellsTheTotalAndTheEstimatedDelivery() {
        service.notifyStatusChange(15L, OrderStatus.PAID);

        ArgumentCaptor<String> body = ArgumentCaptor.forClass(String.class);
        verify(emailService).send(eq("ana@correo.com"), eq("Recibimos el pago de tu orden #15"), body.capture());
        assertTrue(body.getValue().startsWith("Hola, Ana:"));
        assertTrue(body.getValue().contains("$40.000"));
        assertTrue(body.getValue().contains("20/10/2026"));
        assertTrue(body.getValue().contains("- Camisa x 2"));
        assertTrue(body.getValue().contains("https://tienda.test/my-orders/15"));
    }

    @Test
    void everyStatusTheCustomerCaresAboutSendsAnEmail() {
        for (OrderStatus status : List.of(OrderStatus.SHIPPED, OrderStatus.DELIVERED, OrderStatus.CANCELLED,
                OrderStatus.RETURN_REQUESTED, OrderStatus.RETURNED)) {
            service.notifyStatusChange(15L, status);
        }

        verify(emailService, times(5)).send(eq("ana@correo.com"), anyString(), anyString());
    }

    @Test
    void creatingAnOrderSendsNothing() {
        service.notifyStatusChange(15L, OrderStatus.CREATED);

        verify(emailService, never()).send(any(), any(), any());
    }

    @Test
    void customerWithoutEmailIsSkipped() {
        when(customerRepository.findById(7L)).thenReturn(Optional.empty());

        service.notifyStatusChange(15L, OrderStatus.SHIPPED);

        verify(emailService, never()).send(any(), any(), any());
    }
}
