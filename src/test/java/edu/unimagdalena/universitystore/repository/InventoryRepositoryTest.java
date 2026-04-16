package edu.unimagdalena.universitystore.repository;

import edu.unimagdalena.universitystore.entity.Category;
import edu.unimagdalena.universitystore.entity.Inventory;
import edu.unimagdalena.universitystore.entity.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class InventoryRepositoryTest {
    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void shouldFindLowStockProducts() {
        Category category = categoryRepository.save(
                Category.builder()
                        .name("Electronics")
                        .build()
        );

        Product product = productRepository.save(
                Product.builder()
                        .sku("SKU001")
                        .name("Mouse")
                        .price(new BigDecimal("50"))
                        .active(true)
                        .category(category)
                        .build()
        );

        inventoryRepository.save(
                Inventory.builder()
                        .availableStock(2)
                        .minimumStock(5)
                        .product(product)
                        .build()
        );

        List<Inventory> result = inventoryRepository.findLowStockProducts();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(product.getId(), result.get(0).getProduct().getId());
    }
}