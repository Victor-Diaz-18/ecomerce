package edu.unimagdalena.universitystore.controller;

import edu.unimagdalena.universitystore.dto.OrderDtos.*;
import edu.unimagdalena.universitystore.mapper.OrderMapper;
import edu.unimagdalena.universitystore.service.PurchaseOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Validated
public class OrderController {
    private final PurchaseOrderService service;
    private final OrderMapper mapper;

    @PostMapping
    public ResponseEntity<OrderResponse> create(
            @Valid @RequestBody CreateOrderRequest req,
            UriComponentsBuilder uriBuilder) {

        var created = service.create(mapper.toEntity(req));

        var location = uriBuilder
                .path("/api/v1/orders/{id}")
                .buildAndExpand(created.getId())
                .toUri();

        return ResponseEntity.created(location)
                .body(mapper.toResponse(created));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> findAll() {
        var result = service.findAll()
                .stream()
                .map(mapper::toResponse)
                .toList();

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(
                mapper.toResponse(service.findById(id))
        );
    }

    @PatchMapping("/{id}/pay")
    public ResponseEntity<OrderResponse> pay(@PathVariable Long id) {
        var paid = service.payOrder(id);

        return ResponseEntity.ok(
                mapper.toResponse(paid)
        );
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancel(@PathVariable Long id) {
        var cancelled =  service.cancelOrder(id);

        return ResponseEntity.ok(
                mapper.toResponse(cancelled)
        );
    }
}