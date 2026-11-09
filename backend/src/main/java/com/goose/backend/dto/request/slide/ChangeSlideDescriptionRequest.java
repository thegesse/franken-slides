package com.goose.backend.dto.request.slide;

import jakarta.validation.constraints.Size;

public record ChangeSlideDescriptionRequest(
        @Size(max = 255)
        String newDescription
) {
}
