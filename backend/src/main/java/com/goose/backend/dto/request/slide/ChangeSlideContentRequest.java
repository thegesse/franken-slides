package com.goose.backend.dto.request.slide;

import jakarta.validation.constraints.NotBlank;
import tools.jackson.databind.JsonNode;

public record ChangeSlideContentRequest(
        @NotBlank
        JsonNode newContent
) {
}
