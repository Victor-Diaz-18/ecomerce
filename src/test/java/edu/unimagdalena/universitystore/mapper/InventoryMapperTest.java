package edu.unimagdalena.universitystore.mapper;

import edu.unimagdalena.universitystore.dto.InventoryDtos;
import edu.unimagdalena.universitystore.entity.Inventory;
import edu.unimagdalena.universitystore.entity.Product;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.*;

class InventoryMapperTest {
    private final InventoryMapper mapper = Mappers.getMapper(InventoryMapper.class);

    private final Inventory inventory = Inventory.builder()
            .id(1L)
            .availableStock(3)
            .minimumStock(10)
            .product(Product.builder().id(5L).name("Mouse").build())
            .build();

    @Test
    void shouldMapEntityToResponse() {
        InventoryDtos.InventoryResponse result = mapper.toResponse(inventory);

        assertEquals(1L, result.id());
        assertEquals(3, result.availableStock());
        assertEquals(10, result.minimumStock());
        assertEquals(5L, result.productId());
        assertEquals("Mouse", result.productName());
    }

    @Test
    void shouldMapEntityToLowStockResponse() {
        InventoryDtos.LowStockProductResponse result = mapper.toLowStockResponse(inventory);

        assertEquals(5L, result.productId());
        assertEquals("Mouse", result.productName());
        assertEquals(3, result.availableStock());
        assertEquals(10, result.minimumStock());
    }
}
