package edu.unimagdalena.universitystore.controller;

import edu.unimagdalena.universitystore.dto.InventoryDtos.*;
import edu.unimagdalena.universitystore.entity.Inventory;
import edu.unimagdalena.universitystore.entity.Product;
import edu.unimagdalena.universitystore.mapper.InventoryMapper;
import edu.unimagdalena.universitystore.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventories")
@RequiredArgsConstructor
@Validated
public class InventoryController {
    private final InventoryService service;
    private final InventoryMapper mapper;

    @PostMapping
    public ResponseEntity<InventoryResponse> create(
            @Valid @RequestBody CreateInventoryRequest req,
            UriComponentsBuilder uriBuilder) {

        Inventory inventory = new Inventory();
        inventory.setAvailableStock(req.availableStock());
        inventory.setMinimumStock(req.minimumStock());
        Product product = new Product();
        product.setId(req.productId());
        inventory.setProduct(product);

        var created = service.create(inventory);

        var location = uriBuilder
                .path("/api/v1/inventories/{id}")
                .buildAndExpand(created.getId())
                .toUri();

        return ResponseEntity.created(location)
                .body(mapper.toResponse(created));
    }

    @GetMapping
    public ResponseEntity<List<InventoryResponse>> findAll() {
        var result = service.findAll()
                .stream()
                .map(mapper::toResponse)
                .toList();

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventoryResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(
                mapper.toResponse(service.findByProductId(id)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<InventoryResponse> updateStock(
            @PathVariable Long id,
            @Valid @RequestBody UpdateInventoryRequest req) {

        var updated = service.updateStock(
                id,
                req.availableStock(),
                req.minimumStock()
        );

        return ResponseEntity.ok(
                mapper.toResponse(updated)
        );
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<LowStockProductResponse>> lowStock() {
        var result = service.findLowStockProducts()
                .stream()
                .map(mapper::toLowStockResponse)
                .toList();

        return ResponseEntity.ok(result);
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<InventoryResponse> findByProductId(@PathVariable Long productId) {
        return ResponseEntity.ok(
                mapper.toResponse(service.findByProductId(productId))
        );
    }
}
