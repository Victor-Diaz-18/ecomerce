package edu.unimagdalena.universitystore.controller;

import edu.unimagdalena.universitystore.dto.OrderDtos.*;
import edu.unimagdalena.universitystore.enums.OrderStatus;
import edu.unimagdalena.universitystore.service.PurchaseOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Validated
public class OrderController {
    private final PurchaseOrderService service;

    @PostMapping
    public ResponseEntity<OrderResponse> create(
            @Valid @RequestBody CreateOrderRequest req,
            UriComponentsBuilder uriBuilder) {

        var created = service.create(req);

        var location = uriBuilder
                .path("/api/v1/orders/{id}")
                .buildAndExpand(created.id())
                .toUri();

        return ResponseEntity.created(location)
                .body(created);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/search")
    public ResponseEntity<List<OrderResponse>> search(
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) String status) {
        var parsedStatus = status == null ? null : OrderStatus.valueOf(status.toUpperCase());
        return ResponseEntity.ok(service.search(customerId, parsedStatus));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<OrderStatusHistoryResponse>> getHistory(@PathVariable Long id) {
        return ResponseEntity.ok(service.getHistory(id));
    }

    @PatchMapping("/{id}/pay")
    public ResponseEntity<OrderResponse> pay(@PathVariable Long id) {
        return ResponseEntity.ok(service.payOrder(id));
    }

    @PatchMapping("/{id}/ship")
    public ResponseEntity<OrderResponse> ship(@PathVariable Long id) {
        return ResponseEntity.ok(service.shipOrder(id));
    }

    @PatchMapping("/{id}/deliver")
    public ResponseEntity<OrderResponse> deliver(@PathVariable Long id) {
        return ResponseEntity.ok(service.deliverOrder(id));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(service.cancelOrder(id));
    }

    @PatchMapping("/{id}/return")
    public ResponseEntity<OrderResponse> returnOrder(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.get("reason") : null;
        return ResponseEntity.ok(service.returnOrder(id, reason));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDelete(@PathVariable Long id) {
        service.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}