package edu.unimagdalena.universitystore.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.List;

public class AddressDtos {
    public record CreateAddressRequest(
            @NotBlank String street,
            @NotBlank String city,
            @NotBlank String country,
            @NotNull Long customerId,
            String neighborhood
    ) implements Serializable {}

    public record UpdateAddressRequest(
            @NotBlank String street,
            @NotBlank String city,
            String neighborhood
    ) implements Serializable {}

    public record AddressResponse(
            Long id,
            String street,
            String city,
            String country,
            Long customerId,
            String customerName,
            String addressLine,
            String neighborhood,
            String department
    ) implements Serializable {}

    public record DepartmentResponse(
            String name,
            List<String> cities
    ) implements Serializable {}
}