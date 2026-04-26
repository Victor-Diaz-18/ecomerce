package edu.unimagdalena.universitystore.service.Impl;

import edu.unimagdalena.universitystore.entity.Inventory;
import edu.unimagdalena.universitystore.exception.ResourceNotFoundException;
import edu.unimagdalena.universitystore.exception.ValidationException;
import edu.unimagdalena.universitystore.repository.InventoryRepository;
import edu.unimagdalena.universitystore.repository.ProductRepository;
import edu.unimagdalena.universitystore.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {
    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;

    @Override
    public Inventory create(Inventory inventory) {
        if (!productRepository.existsById(inventory.getProduct().getId())) {
            throw new ResourceNotFoundException("Product not found");
        }
        return inventoryRepository.save(inventory);
    }

    @Override
    public Inventory updateStock(Long id, Integer newStock) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found"));

        if (newStock < 0) {
            throw new ValidationException("New stock cannot be negative");
        }

        inventory.setAvailableStock(newStock);

        return inventoryRepository.save(inventory);
    }

    @Override
    public List<Inventory> findLowStockProducts() {
        return inventoryRepository.findLowStockProducts();
    }
}