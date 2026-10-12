package edu.unimagdalena.universitystore.mapper;

import edu.unimagdalena.universitystore.dto.OrderDtos;
import edu.unimagdalena.universitystore.entity.Address;
import edu.unimagdalena.universitystore.entity.Customer;
import edu.unimagdalena.universitystore.entity.OrderItem;
import edu.unimagdalena.universitystore.entity.OrderStatusHistory;
import edu.unimagdalena.universitystore.entity.Product;
import edu.unimagdalena.universitystore.entity.PurchaseOrder;
import edu.unimagdalena.universitystore.enums.OrderStatus;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderMapperTest {
    private final OrderMapper mapper = Mappers.getMapper(OrderMapper.class);

    private static OrderItem item() {
        return OrderItem.builder()
                .id(7L)
                .quantity(2)
                .unitPrice(new BigDecimal("50000"))
                .subtotal(new BigDecimal("100000"))
                .product(Product.builder().id(5L).name("Mouse").build())
                .build();
    }

    @Test
    void shouldMapOrderToResponse() {
        PurchaseOrder order = PurchaseOrder.builder()
                .id(1L)
                .status(OrderStatus.PAID)
                .total(new BigDecimal("100000"))
                .customer(Customer.builder().id(2L).name("Juan").build())
                .address(Address.builder().id(3L).street("Calle 10").city("Santa Marta").country("Colombia").build())
                .items(List.of(item()))
                .build();

        OrderDtos.OrderResponse result = mapper.toResponse(order);

        assertEquals(1L, result.id());
        assertEquals("PAID", result.status());
        assertEquals(2L, result.customerId());
        assertEquals("Juan", result.customerName());
        assertEquals(3L, result.addressId());
        assertEquals("Calle 10, Santa Marta, Colombia", result.addressLine());
        assertEquals(new BigDecimal("100000"), result.total());
        assertEquals(1, result.items().size());
        assertEquals("Mouse", result.items().get(0).productName());
    }

    @Test
    void shouldMapOrderWithoutAddressOrStatus() {
        PurchaseOrder order = PurchaseOrder.builder().id(1L).build();

        OrderDtos.OrderResponse result = mapper.toResponse(order);

        assertNull(result.status());
        assertNull(result.addressLine());
        assertTrue(result.items().isEmpty());
    }

    @Test
    void shouldMapItemToResponse() {
        OrderDtos.OrderItemResponse result = mapper.toItemResponse(item());

        assertEquals(7L, result.id());
        assertEquals(5L, result.productId());
        assertEquals("Mouse", result.productName());
        assertEquals(2, result.quantity());
        assertEquals(new BigDecimal("50000"), result.unitPrice());
        assertEquals(new BigDecimal("100000"), result.subtotal());
    }

    @Test
    void shouldMapHistoryToResponse() {
        LocalDateTime changedAt = LocalDateTime.of(2026, 6, 17, 20, 0);
        OrderStatusHistory history = OrderStatusHistory.builder()
                .id(4L)
                .status(OrderStatus.SHIPPED)
                .changedAt(changedAt)
                .build();

        OrderDtos.OrderStatusHistoryResponse result = mapper.toHistoryResponse(history);

        assertEquals(4L, result.id());
        assertEquals("SHIPPED", result.status());
        assertEquals(changedAt, result.changedAt());
    }
}
