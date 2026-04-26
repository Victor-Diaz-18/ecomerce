package edu.unimagdalena.universitystore.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

public class InventoryDtos {
    public record CreateInventoryRequest(
            @NotNull @Min(0) Integer availableStock,
            @NotNull @Min(0) Integer minimumStock,
            @NotNull Long productId
    )implements  Serializable {}

    public record UpdateInventoryRequest(
            @NotNull @Min(0) Integer availableStock,
            @NotNull @Min(0) Integer minimumStock
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