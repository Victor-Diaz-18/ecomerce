package edu.unimagdalena.universitystore.service;

import edu.unimagdalena.universitystore.entity.Category;
import edu.unimagdalena.universitystore.entity.Product;
import edu.unimagdalena.universitystore.exception.ConflictException;
import edu.unimagdalena.universitystore.exception.ResourceNotFoundException;
import edu.unimagdalena.universitystore.exception.ValidationException;
import edu.unimagdalena.universitystore.repository.CategoryRepository;
import edu.unimagdalena.universitystore.repository.ProductRepository;
import edu.unimagdalena.universitystore.service.Impl.ProductServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {
    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void shouldCreateProduct() {
        Category category = Category.builder()
                .id(1L)
                .build();

        Product product = Product.builder()
                .sku("SKU001")
                .name("Mouse")
                .price(new BigDecimal("50000"))
                .category(category)
                .build();

        when(productRepository.findBySku("SKU001"))
                .thenReturn(Optional.empty());

        when(categoryRepository.existsById(1L))
                .thenReturn(true);

        when(productRepository.save(any(Product.class)))
                .thenAnswer(i -> i.getArgument(0));

        Product result = productService.create(product);

        assertNotNull(result);
        assertEquals("SKU001", result.getSku());
        assertTrue(result.getActive());
    }

    @Test
    void shouldThrowExceptionWhenSkuExists() {
        Product product = Product.builder()
                .sku("SKU001")
                .build();

        when(productRepository.findBySku("SKU001"))
                .thenReturn(Optional.of(product));

        ConflictException exception = assertThrows(ConflictException.class,
                () -> productService.create(product));

        assertEquals("SKU already exists", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenPriceInvalid() {
        Category category = Category.builder()
                .id(1L)
                .build();

        Product product = Product.builder()
                .sku("SKU001")
                .price(BigDecimal.ZERO)
                .category(category)
                .build();

        when(productRepository.findBySku("SKU001"))
                .thenReturn(Optional.empty());

        ValidationException exception = assertThrows(ValidationException.class,
                () -> productService.create(product));

        assertEquals("Price must be greater than zero", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenCategoryNotFound() {
        Category category = Category.builder()
                .id(1L)
                .build();

        Product product = Product.builder()
                .sku("SKU001")
                .price(new BigDecimal("50000"))
                .category(category)
                .build();

        when(productRepository.findBySku("SKU001"))
                .thenReturn(Optional.empty());

        when(categoryRepository.existsById(1L))
                .thenReturn(false);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> productService.create(product));

        assertEquals("Category not found", exception.getMessage());
    }

    @Test
    void shouldFindAllProducts() {
        when(productRepository.findAll())
                .thenReturn(List.of(new Product(), new Product()));

        List<Product> result = productService.findAll();

        assertEquals(2, result.size());
    }

    @Test
    void shouldFindProductById() {
        Product product = Product.builder()
                .id(1L)
                .name("Keyboard")
                .build();

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        Product result = productService.findById(1L);

        assertEquals(1L, result.getId());
    }

    @Test
    void shouldThrowExceptionWhenProductNotFound() {
        when(productRepository.findById(1L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> productService.findById(1L));

        assertEquals("Product not found", exception.getMessage());
    }

    @Test
    void shouldFindProductsByCategory() {
        when(productRepository.findByCategoryIdAndActiveTrue(1L))
                .thenReturn(List.of(new Product()));

        List<Product> result = productService.findByCategory(1L);

        assertEquals(1, result.size());
    }

    @Test
    void shouldUpdateProduct() {
        Category category = Category.builder().id(1L).build();

        Product product = Product.builder()
                .id(1L)
                .sku("SKU001")
                .name("Mouse")
                .price(new BigDecimal("50000"))
                .category(category)
                .build();
        Product updated = Product.builder()
                .sku("SKU002")
                .name("Keyboard")
                .price(new BigDecimal("80000"))
                .build();

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(productRepository.save(any(Product.class)))
                .thenAnswer(i -> i.getArgument(0));

        Product result = productService.update(1L, updated);

        assertEquals("Keyboard", result.getName());
        assertEquals("SKU002", result.getSku());
        assertEquals(new BigDecimal("80000"), result.getPrice());
        assertEquals(1L, result.getId());
    }
}