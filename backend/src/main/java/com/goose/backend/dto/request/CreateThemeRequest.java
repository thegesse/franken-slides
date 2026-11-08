package com.goose.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateThemeRequest(
        @NotBlank
        @Size(max = 255)
        String title
) {}
