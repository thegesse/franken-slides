package com.goose.backend.dto.request.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank @Size(max = 255)
        String username,

        @NotBlank @Size(min = 8, message = "Password must be at least 8 characters long")
        String password
) {
}
