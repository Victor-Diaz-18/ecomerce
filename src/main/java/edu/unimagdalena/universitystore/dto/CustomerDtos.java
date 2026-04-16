package edu.unimagdalena.universitystore.dto;

import edu.unimagdalena.universitystore.enums.CustomerStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;

public class CustomerDtos {
    public record CreateCustomerRequest(
            @NotBlank String name,
            @NotBlank @Email String email
    ) implements Serializable {}

    public record UpdateCustomerRequest(
            @NotBlank String name,
            @NotBlank @Email String email
    ) implements Serializable {}

    public record CustomerResponse(
            Long id,
            String name,
            String email,
            CustomerStatus status
    ) implements Serializable {}
}