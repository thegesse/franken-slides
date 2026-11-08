package com.goose.backend.dto.request.theme;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateThemeRequest(
        @NotBlank
        @Size(max = 255)
        String newTitle
) {
}
