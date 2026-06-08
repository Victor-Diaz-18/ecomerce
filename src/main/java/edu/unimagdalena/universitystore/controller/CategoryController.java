package edu.unimagdalena.universitystore.controller;

import edu.unimagdalena.universitystore.dto.CategoryDtos.*;
import edu.unimagdalena.universitystore.entity.Category;
import edu.unimagdalena.universitystore.mapper.CategoryMapper;
import edu.unimagdalena.universitystore.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Validated
public class CategoryController {
    private final CategoryService service;
    private final CategoryMapper mapper;

    @PostMapping
    public ResponseEntity<CategoryResponse> create(
            @Valid @RequestBody CreateCategoryRequest req,
            UriComponentsBuilder uriBuilder) {

        Category category = new Category();
        category.setName(req.name());

        var created = service.create(category);

        var location = uriBuilder
                .path("/api/v1/categories/{id}")
                .buildAndExpand(created.getId())
                .toUri();

        return ResponseEntity.created(location)
                .body(mapper.toResponse(created));
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> findAll() {
        var result = service.findAll()
                .stream()
                .map(mapper::toResponse)
                .toList();

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(
                mapper.toResponse(service.findById(id))
        );
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CategoryResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCategoryRequest req) {

        Category category = service.findById(id);
        category.setName(req.name());

        var updated = service.update(id, category);

        return ResponseEntity.ok(mapper.toResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}