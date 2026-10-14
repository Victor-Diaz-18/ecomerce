package edu.unimagdalena.universitystore.dto;

import edu.unimagdalena.universitystore.dto.OrderDtos.CreateOrderItemRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.util.List;

/**
 * Peticiones del portal del cliente. No llevan el id del cliente: sale de la sesión iniciada.
 */
public class AccountDtos {
    public record MyAddressRequest(
            @NotBlank String street,
            @NotBlank String city,
            String neighborhood
    ) implements Serializable {}

    public record CreateMyOrderRequest(
            @NotNull Long addressId,
            @NotEmpty List<@Valid CreateOrderItemRequest> items
    ) implements Serializable {}

    /** "Comprar ahora": un solo producto, que se paga en el mismo paso. */
    public record BuyNowRequest(
            @NotNull Long addressId,
            @NotNull Long productId,
            @NotNull @Min(1) Integer quantity
    ) implements Serializable {}

    public record MyProfileRequest(
            @NotBlank @Size(max = 100) String name
    ) implements Serializable {}

    public record ChangePasswordRequest(
            @NotBlank String currentPassword,
            @NotBlank @Size(min = 8) String newPassword
    ) implements Serializable {}

    public record ChangeAddressRequest(
            @NotNull Long addressId
    ) implements Serializable {}

    public record ReturnRequest(
            String reason
    ) implements Serializable {}
}
