package edu.unimagdalena.universitystore.dto;

import edu.unimagdalena.universitystore.dto.OrderDtos.CreateOrderItemRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

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

    public record ChangeAddressRequest(
            @NotNull Long addressId
    ) implements Serializable {}

    public record ReturnRequest(
            String reason
    ) implements Serializable {}
}
