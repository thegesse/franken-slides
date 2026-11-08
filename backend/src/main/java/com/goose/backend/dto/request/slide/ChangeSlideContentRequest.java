package com.goose.backend.dto.request.slide;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import tools.jackson.databind.JsonNode;

public record ChangeSlideContentRequest(
        @NotNull
        JsonNode newContent
) {
}
