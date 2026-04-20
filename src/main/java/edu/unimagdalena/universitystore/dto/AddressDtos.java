package edu.unimagdalena.universitystore.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

public class AddressDtos {
    public record CreateAddressRequest(
            @NotBlank String street,
            @NotBlank String city,
            @NotBlank String country,
            @NotNull Long customerId
    ) implements Serializable {}

    public record AddressResponse(
            Long id,
            String street,
            String city,
            String country,
            Long customerId
    ) implements Serializable {}
}