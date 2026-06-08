package edu.unimagdalena.universitystore.controller;

import edu.unimagdalena.universitystore.dto.CustomerDtos.*;
import edu.unimagdalena.universitystore.entity.Customer;
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

        Customer customer = new Customer();
        customer.setName(req.name());
        customer.setEmail(req.email());

        var created = service.create(customer);

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

        Customer customer = service.findById(id);
        customer.setName(req.name());
        customer.setEmail(req.email());
        if (req.status() != null) {
            customer.setStatus(req.status());
        }

        var updated = service.update(id, customer);

        return ResponseEntity.ok(
                mapper.toResponse(updated)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}