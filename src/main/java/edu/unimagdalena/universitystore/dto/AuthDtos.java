package edu.unimagdalena.universitystore.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

public class AuthDtos {
    public record RegisterRequest(
            @NotBlank String name,
            @Email @NotBlank String email,
            @Size(min = 8) @NotBlank String password
    ) implements Serializable {}

    public record LoginRequest(
            @Email @NotBlank String email,
            @NotBlank String password
    ) implements Serializable {}

    public record AuthResponse(
            String token,
            String email,
            java.util.Set<String> roles
    ) implements Serializable {

    }
}