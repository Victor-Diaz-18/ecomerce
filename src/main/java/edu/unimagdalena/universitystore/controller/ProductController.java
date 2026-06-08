package edu.unimagdalena.universitystore.controller;

import edu.unimagdalena.universitystore.dto.ProductDtos.*;
import edu.unimagdalena.universitystore.entity.Product;
import edu.unimagdalena.universitystore.entity.Category;
import edu.unimagdalena.universitystore.mapper.ProductMapper;
import edu.unimagdalena.universitystore.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Validated
public class ProductController {
    private final ProductService service;
    private final ProductMapper mapper;

    @PostMapping
    public ResponseEntity<ProductResponse> create(
            @Valid @RequestBody CreateProductRequest req,
            UriComponentsBuilder uriBuilder) {

        Product product = new Product();
        product.setName(req.name());
        product.setSku(req.sku());
        product.setPrice(req.price());
        product.setActive(true);
        Category category = new Category();
        category.setId(req.categoryId());
        product.setCategory(category);

        var created = service.create(product);

        var location = uriBuilder
                .path("/api/v1/products/{id}")
                .buildAndExpand(created.getId())
                .toUri();

        return ResponseEntity.created(location)
                .body(mapper.toResponse(created));
    }

    @GetMapping
    public ResponseEntity<Page<ProductResponse>> findAll(
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        var result = service.findAll(pageable)
                .map(mapper::toResponse);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/all")
    public ResponseEntity<List<ProductResponse>> findAllList() {
        var result = service.findAll()
                .stream()
                .map(mapper::toResponse)
                .toList();

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(
                mapper.toResponse(service.findById(id))
        );
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ProductResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequest req) {

        Product product = service.findById(id);
        product.setName(req.name());
        product.setSku(req.sku());
        product.setPrice(req.price());
        if (req.active() != null) {
            product.setActive(req.active());
        }
        if (req.categoryId() != null) {
            Category category = new Category();
            category.setId(req.categoryId());
            product.setCategory(category);
        }

        var updated = service.update(id, product);

        return ResponseEntity.ok(
                mapper.toResponse(updated)
        );
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProductResponse>> findByCategory(@PathVariable Long categoryId) {
        var result = service.findByCategory(categoryId)
                .stream()
                .map(mapper::toResponse)
                .toList();

        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}