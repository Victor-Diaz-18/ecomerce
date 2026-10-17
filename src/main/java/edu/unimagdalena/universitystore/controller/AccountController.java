package edu.unimagdalena.universitystore.controller;

import edu.unimagdalena.universitystore.dto.AccountDtos.*;
import edu.unimagdalena.universitystore.dto.AddressDtos.AddressResponse;
import edu.unimagdalena.universitystore.dto.CustomerDtos.CustomerResponse;
import edu.unimagdalena.universitystore.dto.OrderDtos.OrderResponse;
import edu.unimagdalena.universitystore.dto.OrderDtos.OrderStatusHistoryResponse;
import edu.unimagdalena.universitystore.entity.User;
import edu.unimagdalena.universitystore.mapper.AddressMapper;
import edu.unimagdalena.universitystore.mapper.CustomerMapper;
import edu.unimagdalena.universitystore.security.service.AuthService;
import edu.unimagdalena.universitystore.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Portal del cliente: todo se refiere al cliente que inició sesión, nunca a un id que venga en la petición.
 */
@RestController
@RequestMapping("/api/v1/me")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService service;
    private final CustomerMapper customerMapper;
    private final AddressMapper addressMapper;
    private final AuthService authService;

    @GetMapping
    public ResponseEntity<CustomerResponse> profile(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(customerMapper.toResponse(service.profile(customerId(user))));
    }

    @PutMapping
    public ResponseEntity<CustomerResponse> updateProfile(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody MyProfileRequest req) {
        return ResponseEntity.ok(customerMapper.toResponse(service.updateName(customerId(user), req.name())));
    }

    @PatchMapping("/password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ChangePasswordRequest req) {
        authService.changePassword(user.getId(), req.currentPassword(), req.newPassword());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/addresses")
    public ResponseEntity<List<AddressResponse>> addresses(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(service.addresses(customerId(user)).stream().map(addressMapper::toResponse).toList());
    }

    @PostMapping("/addresses")
    public ResponseEntity<AddressResponse> addAddress(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody MyAddressRequest req) {
        var created = service.addAddress(customerId(user), req.street(), req.neighborhood(), req.city());
        return ResponseEntity.status(HttpStatus.CREATED).body(addressMapper.toResponse(created));
    }

    @PutMapping("/addresses/{id}")
    public ResponseEntity<AddressResponse> updateAddress(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @Valid @RequestBody MyAddressRequest req) {
        var updated = service.updateAddress(customerId(user), id, req.street(), req.neighborhood(), req.city());
        return ResponseEntity.ok(addressMapper.toResponse(updated));
    }

    @GetMapping("/orders")
    public ResponseEntity<List<OrderResponse>> orders(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(service.orders(customerId(user)));
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<OrderResponse> order(@AuthenticationPrincipal User user, @PathVariable Long id) {
        return ResponseEntity.ok(service.order(customerId(user), id));
    }

    @PostMapping("/orders")
    public ResponseEntity<OrderResponse> createOrder(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateMyOrderRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createOrder(customerId(user), req));
    }

    @GetMapping("/orders/{id}/history")
    public ResponseEntity<List<OrderStatusHistoryResponse>> orderHistory(
            @AuthenticationPrincipal User user, @PathVariable Long id) {
        return ResponseEntity.ok(service.orderHistory(customerId(user), id));
    }

    @PostMapping("/orders/buy-now")
    public ResponseEntity<OrderResponse> buyNow(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody BuyNowRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.buyNow(customerId(user), req));
    }

    @PatchMapping("/orders/{id}/pay")
    public ResponseEntity<OrderResponse> pay(@AuthenticationPrincipal User user, @PathVariable Long id) {
        return ResponseEntity.ok(service.payOrder(customerId(user), id));
    }

    @PatchMapping("/orders/{id}/cancel")
    public ResponseEntity<OrderResponse> cancel(@AuthenticationPrincipal User user, @PathVariable Long id) {
        return ResponseEntity.ok(service.cancelOrder(customerId(user), id));
    }

    @PatchMapping("/orders/{id}/return")
    public ResponseEntity<OrderResponse> returnOrder(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @RequestBody(required = false) ReturnRequest req) {
        return ResponseEntity.ok(service.returnOrder(customerId(user), id, req != null ? req.reason() : null));
    }

    @PatchMapping("/orders/{id}/address")
    public ResponseEntity<OrderResponse> changeAddress(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @Valid @RequestBody ChangeAddressRequest req) {
        return ResponseEntity.ok(service.changeOrderAddress(customerId(user), id, req.addressId()));
    }

    // The security rules only let customers in, and every customer account has a customer file
    private static Long customerId(User user) {
        return user.getCustomer().getId();
    }
}
