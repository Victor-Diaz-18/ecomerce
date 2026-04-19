package edu.unimagdalena.universitystore.service;

import edu.unimagdalena.universitystore.entity.Inventory;
import edu.unimagdalena.universitystore.entity.Product;
import edu.unimagdalena.universitystore.exception.ResourceNotFoundException;
import edu.unimagdalena.universitystore.exception.ValidationException;
import edu.unimagdalena.universitystore.repository.InventoryRepository;
import edu.unimagdalena.universitystore.repository.ProductRepository;
import edu.unimagdalena.universitystore.service.Impl.InventoryServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceImplTest {
    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    @Test
    void shouldCreateInventory() {
        Product product = Product.builder()
                .id(1L)
                .build();

        Inventory inventory = Inventory.builder()
                .availableStock(10)
                .minimumStock(5)
                .product(product)
                .build();

        when(productRepository.existsById(1L)).thenReturn(true);
        when(inventoryRepository.save(inventory)).thenReturn(inventory);

        Inventory result = inventoryService.create(inventory);

        assertNotNull(result);
        assertEquals(10, result.getAvailableStock());
        verify(inventoryRepository).save(inventory);
    }

    @Test
    void shouldThrowExceptionWhenProductNotFound() {
        Product product = Product.builder()
                .id(1L)
                .build();

        Inventory inventory = Inventory.builder()
                .product(product)
                .build();

        when(productRepository.existsById(1L)).thenReturn(false);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> inventoryService.create(inventory));

        assertEquals("Product not found", exception.getMessage());
    }

    @Test
    void shouldUpdateStock() {
        Inventory inventory = Inventory.builder()
                .id(1L)
                .availableStock(5)
                .build();

        when(inventoryRepository.findById(1L))
                .thenReturn(Optional.of(inventory));

        when(inventoryRepository.save(any(Inventory.class)))
                .thenReturn(inventory);

        Inventory result = inventoryService.updateStock(1L, 20);

        assertEquals(20, result.getAvailableStock());
    }

    @Test
    void shouldThrowExceptionWhenInventoryNotFound() {
        when(inventoryRepository.findById(1L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> inventoryService.updateStock(1L, 20));

        assertEquals("Inventory not found", exception.getMessage());
    }

    @Test
    void shouldFindLowStockProducts() {
        when(inventoryRepository.findLowStockProducts())
                .thenReturn(List.of(new Inventory()));

        List<Inventory> result = inventoryService.findLowStockProducts();

        assertEquals(1, result.size());
    }

    @Test
    void shouldThrowExceptionWhenStockNegative() {
        Inventory inventory = Inventory.builder()
                .id(1L)
                .availableStock(10)
                .build();

        when(inventoryRepository.findById(1L))
                .thenReturn(Optional.of(inventory));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> inventoryService.updateStock(1L, -5));

        assertEquals("New stock cannot be negative", exception.getMessage());
    }
}