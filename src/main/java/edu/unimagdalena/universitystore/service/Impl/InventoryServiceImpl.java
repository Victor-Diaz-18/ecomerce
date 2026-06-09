package edu.unimagdalena.universitystore.service.Impl;

import edu.unimagdalena.universitystore.entity.Inventory;
import edu.unimagdalena.universitystore.entity.Product;
import edu.unimagdalena.universitystore.exception.ResourceNotFoundException;
import edu.unimagdalena.universitystore.exception.ValidationException;
import edu.unimagdalena.universitystore.repository.InventoryRepository;
import edu.unimagdalena.universitystore.repository.ProductRepository;
import edu.unimagdalena.universitystore.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {
    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public Inventory create(Inventory inventory) {
        if (inventory.getProduct() == null || inventory.getProduct().getId() == null) {
            throw new ResourceNotFoundException("Product not found");
        }
        Product product = productRepository.findById(inventory.getProduct().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        inventory.setProduct(product);
        return inventoryRepository.save(inventory);
    }

    @Override
    @Transactional
    public Inventory updateStock(Long id, Integer newStock, Integer newMinimumStock) {
        if (newStock == null || newStock < 0) {
            throw new ValidationException("New stock cannot be null or negative");
        }
        if (newMinimumStock == null || newMinimumStock < 0) {
            throw new ValidationException("Minimum stock cannot be null or negative");
        }

        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found"));

        inventory.setAvailableStock(newStock);
        inventory.setMinimumStock(newMinimumStock);

        return inventoryRepository.save(inventory);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Inventory> findLowStockProducts() {
        return inventoryRepository.findLowStockProducts();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Inventory> findAll() {
        return inventoryRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Inventory findByProductId(Long productId) {
        return inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found"));
    }
}
