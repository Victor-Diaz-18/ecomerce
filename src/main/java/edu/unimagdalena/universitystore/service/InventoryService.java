package edu.unimagdalena.universitystore.service;

import edu.unimagdalena.universitystore.entity.Inventory;

import java.util.List;

public interface InventoryService {
    Inventory create(Inventory inventory);
    Inventory updateStock(Long id, Integer newStock);
    List<Inventory> findLowStockProducts();
}