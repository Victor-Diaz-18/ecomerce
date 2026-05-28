package edu.unimagdalena.universitystore.controller;

import edu.unimagdalena.universitystore.dto.AddressDtos.*;
import edu.unimagdalena.universitystore.mapper.AddressMapper;
import edu.unimagdalena.universitystore.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/v1/addresses")
@RequiredArgsConstructor
@Validated
public class AddressController {
    private final AddressService service;
    private final AddressMapper mapper;

    @PostMapping
    public ResponseEntity<AddressResponse> create(
            @Valid @RequestBody CreateAddressRequest req,
            UriComponentsBuilder uriBuilder) {
        var created = service.create(mapper.toEntity(req));

        var location = uriBuilder
                .path("/api/v1/addresses/{id}")
                .buildAndExpand(created.getId())
                .toUri();

        return ResponseEntity.created(location)
                .body(mapper.toResponse(created));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<AddressResponse>> findByCustomer(
            @PathVariable Long customerId) {
        var result = service.findByCustomer(customerId)
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

    @GetMapping("/{id}")
    public ResponseEntity<AddressResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(
                mapper.toResponse(service.findById(id)));
    }
}