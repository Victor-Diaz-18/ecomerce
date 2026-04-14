package edu.unimagdalena.universitystore.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderDtos {

    public record CreateOrderRequest(
            Long customerId,
            Long addressId,
            List<CreateOrderItemRequest> items
    ) implements Serializable {}

    public record CreateOrderItemRequest(
            Long productId,
            @NotNull @Min(1) Integer quantity
    ) implements Serializable {}

    public record CancelOrderRequest(
            String reason
    ) implements Serializable {}

    public record OrderItemResponse(
            Long id,
            Long productId,
            Integer quantity,
            BigDecimal unitPrice
    ) implements Serializable {}

    public record OrderResponse(
            Long id,
            String status,
            LocalDateTime createdAt,
            Long customerId,
            List<OrderItemResponse> items
    ) implements Serializable {}

    public record BestSellingProductResponse(
            String productName,
            Long totalSold
    ) implements Serializable {}

    public record MonthlyIncomeResponse(
            Integer month,
            BigDecimal totalIncome
    ) implements Serializable {}

    public record TopCustomerResponse(
            String customerName,
            Long totalOrders
    ) implements Serializable {}
}