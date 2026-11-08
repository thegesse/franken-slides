package com.goose.backend.dto.request.slide;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import tools.jackson.databind.JsonNode;

public record CreateSlideRequest(
        @NotBlank
        @Size(max = 255)
        String title,

        @Size(max = 255)
        String description,

        @NotNull
        JsonNode content
) {
}
