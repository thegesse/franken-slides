package com.goose.backend.dto.request.slideGroup;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSlideGroupRequest(
        @NotBlank
        @Size(max = 255)
        String name
) {
}
