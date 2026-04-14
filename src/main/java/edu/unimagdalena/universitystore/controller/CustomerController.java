package edu.unimagdalena.universitystore.controller;

import edu.unimagdalena.universitystore.dto.CustomerDtos.*;
import edu.unimagdalena.universitystore.mapper.CustomerMapper;
import edu.unimagdalena.universitystore.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Validated
public class CustomerController {

    private final CustomerService service;
    private final CustomerMapper mapper;

    @PostMapping
    public ResponseEntity<CustomerResponse> create(
            @Valid @RequestBody CreateCustomerRequest req,
            UriComponentsBuilder uriBuilder) {

        var created = service.create(mapper.toEntity(req));

        var location = uriBuilder
                .path("/api/v1/customers/{id}")
                .buildAndExpand(created.getId())
                .toUri();

        return ResponseEntity.created(location)
                .body(mapper.toResponse(created));
    }

    @GetMapping
    public ResponseEntity<List<CustomerResponse>> findAll() {
        var result = service.findAll()
                .stream()
                .map(mapper::toResponse)
                .toList();

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(
                mapper.toResponse(service.findById(id))
        );
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CustomerResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCustomerRequest req) {

        var updated = service.update(id, mapper.toEntity(req));

        return ResponseEntity.ok(
                mapper.toResponse(updated)
        );
    }
}