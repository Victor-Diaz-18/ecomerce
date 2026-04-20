package edu.unimagdalena.universitystore.controller;

import edu.unimagdalena.universitystore.dto.InventoryDtos.*;
import edu.unimagdalena.universitystore.mapper.InventoryMapper;
import edu.unimagdalena.universitystore.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
            @Valid @RequestBody CreateInventoryRequest req) {
        var created = service.create(mapper.toEntity(req));

        return ResponseEntity.status(201)
                .body(mapper.toResponse(created));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<InventoryResponse> updateStock(
            @PathVariable Long id,
            @Valid @RequestBody UpdateInventoryRequest req) {

        var updated = service.updateStock(
                id,
                req.availableStock()
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
}