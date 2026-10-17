package edu.unimagdalena.universitystore.service;

import edu.unimagdalena.universitystore.dto.OrderDtos.OrderResponse;
import edu.unimagdalena.universitystore.entity.Customer;
import edu.unimagdalena.universitystore.enums.OrderStatus;
import edu.unimagdalena.universitystore.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * El correo que recibe el cliente cuando su orden cambia de estado. No todos los cambios avisan:
 * solo los que le importan al cliente (pago, envío, entrega, cancelación y devoluciones).
 */
@Service
@RequiredArgsConstructor
public class OrderNotificationService {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final PurchaseOrderService orderService;
    private final CustomerRepository customerRepository;
    private final EmailService emailService;

    @Value("${store.public-url}")
    private String storeUrl;

    @Transactional(readOnly = true)
    public void notifyStatusChange(Long orderId, OrderStatus newStatus) {
        OrderResponse order = orderService.findById(orderId);
        Customer customer = customerRepository.findById(order.customerId()).orElse(null);
        if (customer == null || customer.getEmail() == null) {
            return;
        }

        String subject;
        String message;
        switch (newStatus) {
            case PAID -> {
                subject = "Recibimos el pago de tu orden #" + order.id();
                message = "Tu pago de " + money(order.total()) + " fue recibido."
                        + (order.estimatedDelivery() != null
                        ? " Te llegará aproximadamente el " + order.estimatedDelivery().format(DATE) + "." : "");
            }
            case SHIPPED -> {
                subject = "Tu orden #" + order.id() + " va en camino";
                message = "Despachamos tu orden a: " + order.addressLine() + ".";
            }
            case DELIVERED -> {
                subject = "Tu orden #" + order.id() + " fue entregada";
                message = "¡Esperamos que la disfrutes! Si algo no está bien, puedes pedir una devolución desde Mis órdenes.";
            }
            case CANCELLED -> {
                subject = "Tu orden #" + order.id() + " fue cancelada";
                message = "La orden se canceló y los productos volvieron a la tienda.";
            }
            case RETURN_REQUESTED -> {
                subject = "Recibimos tu solicitud de devolución de la orden #" + order.id();
                message = "Revisaremos el producto cuando lo recibamos y te avisaremos el resultado.";
            }
            case RETURNED -> {
                subject = "Devolución de la orden #" + order.id() + " aceptada";
                message = "Te reembolsaremos " + money(order.total()) + ".";
            }
            default -> {
                return;
            }
        }

        emailService.send(customer.getEmail(), subject, body(customer.getName(), message, order));
    }

    private String body(String name, String message, OrderResponse order) {
        String items = order.items().stream()
                .map(item -> "- " + item.productName() + " x " + item.quantity())
                .collect(Collectors.joining("\n"));
        return "Hola, " + name + ":\n\n"
                + message + "\n\n"
                + "Productos:\n" + items + "\n"
                + "Total: " + money(order.total()) + "\n\n"
                + "Ver tu orden: " + storeUrl + "/my-orders/" + order.id() + "\n\n"
                + "University Store";
    }

    private static String money(BigDecimal value) {
        NumberFormat format = NumberFormat.getNumberInstance(Locale.of("es", "CO"));
        format.setMaximumFractionDigits(0);
        return "$" + format.format(value == null ? BigDecimal.ZERO : value);
    }
}
