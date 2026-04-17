package edu.unimagdalena.universitystore.mapper;

import edu.unimagdalena.universitystore.dto.ProductDtos;
import edu.unimagdalena.universitystore.entity.Category;
import edu.unimagdalena.universitystore.entity.Product;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ProductMapperTest {
    private final ProductMapper mapper = Mappers.getMapper(ProductMapper.class);

    @Test
    void shouldMapCreateRequestToEntity() {
        ProductDtos.CreateProductRequest request =
                new ProductDtos.CreateProductRequest(
                        "Mouse",
                        "SKU001",
                        new BigDecimal("50000"),
                        1L
                );

        Product result = mapper.toEntity(request);

        assertEquals("Mouse", result.getName());
        assertEquals("SKU001", result.getSku());
        assertEquals(1L, result.getCategory().getId());
    }

    @Test
    void shouldMapEntityToResponse() {
        Product product = Product.builder()
                .id(1L)
                .sku("SKU001")
                .name("Mouse")
                .price(new BigDecimal("50000"))
                .active(true)
                .category(Category.builder().id(1L).build())
                .build();

        ProductDtos.ProductResponse result = mapper.toResponse(product);

        assertEquals(1L, result.categoryId());
    }
}