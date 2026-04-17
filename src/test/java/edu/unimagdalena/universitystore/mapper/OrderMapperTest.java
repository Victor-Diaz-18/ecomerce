package edu.unimagdalena.universitystore.mapper;

import edu.unimagdalena.universitystore.dto.OrderDtos;
import edu.unimagdalena.universitystore.entity.Customer;
import edu.unimagdalena.universitystore.entity.OrderItem;
import edu.unimagdalena.universitystore.entity.Product;
import edu.unimagdalena.universitystore.entity.PurchaseOrder;
import edu.unimagdalena.universitystore.enums.OrderStatus;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class OrderMapperTest {
    private final OrderMapper mapper = Mappers.getMapper(OrderMapper.class);

    @Test
    void shouldMapCreateOrderRequestToEntity() {
        OrderDtos.CreateOrderRequest request =
                new OrderDtos.CreateOrderRequest(
                        1L,
                        2L,
                        java.util.List.of()
                );

        PurchaseOrder result = mapper.toEntity(request);

        assertEquals(1L, result.getCustomer().getId());
        assertEquals(2L, result.getAddress().getId());
    }

    @Test
    void shouldMapOrderToResponse() {
        PurchaseOrder order = PurchaseOrder.builder()
                .id(1L)
                .createdAt(LocalDateTime.now())
                .status(OrderStatus.CREATED)
                .total(new BigDecimal("100"))
                .customer(Customer.builder().id(1L).build())
                .build();

        OrderDtos.OrderResponse result = mapper.toResponse(order);

        assertEquals(1L, result.customerId());
    }

    @Test
    void shouldMapOrderItemToResponse() {
        OrderItem item = OrderItem.builder()
                .quantity(2)
                .unitPrice(new BigDecimal("50"))
                .subtotal(new BigDecimal("100"))
                .product(Product.builder().id(1L).build())
                .build();

        OrderDtos.OrderItemResponse result = mapper.toItemResponse(item);

        assertEquals(1L, result.productId());
    }
}