package edu.unimagdalena.universitystore.mapper;

import edu.unimagdalena.universitystore.dto.InventoryDtos;
import edu.unimagdalena.universitystore.entity.Inventory;
import edu.unimagdalena.universitystore.entity.Product;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.*;

class InventoryMapperTest {
    private final InventoryMapper mapper = Mappers.getMapper(InventoryMapper.class);

    @Test
    void shouldMapInventoryToResponse() {
        Inventory inventory = Inventory.builder()
                .id(1L)
                .availableStock(10)
                .minimumStock(5)
                .product(Product.builder().id(1L).build())
                .build();

        InventoryDtos.InventoryResponse result = mapper.toResponse(inventory);

        assertEquals(1L, result.productId());
        assertEquals(10, result.availableStock());
    }

    @Test
    void shouldMapInventoryToLowStockResponse() {
        Inventory inventory = Inventory.builder()
                .id(1L)
                .availableStock(2)
                .minimumStock(5)
                .product(Product.builder()
                        .id(1L)
                        .name("Mouse")
                        .build())
                .build();

        InventoryDtos.LowStockProductResponse result =
                mapper.toLowStockResponse(inventory);

        assertEquals(1L, result.productId());
        assertEquals("Mouse", result.productName());
    }
}