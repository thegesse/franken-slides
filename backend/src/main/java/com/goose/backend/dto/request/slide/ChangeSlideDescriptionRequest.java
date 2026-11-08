package com.goose.backend.dto.request.slide;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangeSlideDescriptionRequest(
        @NotBlank
        @Size(max = 255)
        String newDescription
) {
}
