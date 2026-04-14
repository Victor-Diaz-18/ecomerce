package edu.unimagdalena.universitystore.repository;

import edu.unimagdalena.universitystore.entity.Category;
import edu.unimagdalena.universitystore.entity.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProductRepositoryTest {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void shouldFindProductBySku() {
        Category category = categoryRepository.save(
                Category.builder()
                        .name("Electronics")
                        .build()
        );

        Product product = productRepository.save(
                Product.builder()
                        .sku("ABC123")
                        .name("Laptop")
                        .price(new BigDecimal("2500"))
                        .active(true)
                        .category(category)
                        .build()
        );

        Optional<Product> found = productRepository.findBySku("ABC123");

        assertTrue(found.isPresent());
        assertEquals("Laptop", found.get().getName());
    }
}