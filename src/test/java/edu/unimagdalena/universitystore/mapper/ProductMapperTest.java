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
    void shouldMapEntityToResponse() {
        Product product = Product.builder()
                .id(1L)
                .sku("SKU001")
                .name("Mouse")
                .price(new BigDecimal("50000"))
                .active(true)
                .category(Category.builder().id(2L).name("Tecnologia").build())
                .build();

        ProductDtos.ProductResponse result = mapper.toResponse(product);

        assertEquals(1L, result.id());
        assertEquals("Mouse", result.name());
        assertEquals("SKU001", result.sku());
        assertEquals(new BigDecimal("50000"), result.price());
        assertTrue(result.active());
        assertEquals(2L, result.categoryId());
        assertEquals("Tecnologia", result.categoryName());
    }
}
