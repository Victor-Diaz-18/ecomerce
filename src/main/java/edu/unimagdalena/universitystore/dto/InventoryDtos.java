package edu.unimagdalena.universitystore.dto;

import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

public class InventoryDtos {

    public record UpdateInventoryRequest(
            @NotNull Integer availableStock,
            @NotNull Integer minimumStock
    ) implements Serializable {}

    public record InventoryResponse(
            Long id,
            Integer availableStock,
            Integer minimumStock,
            Long productId
    ) implements Serializable {}

    public record LowStockProductResponse(
            Long productId,
            String productName,
            Integer availableStock,
            Integer minimumStock
    ) implements Serializable {}
}