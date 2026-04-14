package edu.unimagdalena.universitystore.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.math.BigDecimal;

public class ProductDtos {

    public record CreateProductRequest(
            @NotBlank String name,
            @NotBlank String sku,
            @NotNull @DecimalMin("0.01") BigDecimal price,
            @NotNull Long categoryId
    ) implements Serializable {}

    public record UpdateProductRequest(
            @NotBlank String name,
            @NotNull @DecimalMin("0.01") BigDecimal price,
            @NotNull Boolean active

    ) implements Serializable {}

    public record ProductResponse(
            Long id,
            String name,
            String sku,
            BigDecimal price,
            Boolean active,
            Long categoryId
    ) implements Serializable {}
}