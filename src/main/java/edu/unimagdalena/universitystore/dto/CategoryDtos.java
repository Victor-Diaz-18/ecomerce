package edu.unimagdalena.universitystore.dto;

import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;

public class CategoryDtos {

    public record CreateCategoryRequest(
            @NotBlank String name
    ) implements Serializable {}

    public record CategoryResponse(
            Long id,
            String name
    ) implements Serializable {}
}