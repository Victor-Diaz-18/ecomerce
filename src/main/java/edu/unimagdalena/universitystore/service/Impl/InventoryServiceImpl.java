package edu.unimagdalena.universitystore.service.Impl;

import edu.unimagdalena.universitystore.entity.Inventory;
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
            throw new RuntimeException("Product not found");
        }
        return inventoryRepository.save(inventory);
    }

    @Override
    public Inventory updateStock(Long id, Integer newStock) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inventory not found"));

        inventory.setAvailableStock(newStock);

        return inventoryRepository.save(inventory);
    }

    @Override
    public List<Inventory> findLowStockProducts() {
        return inventoryRepository.findLowStockProducts();
    }
}