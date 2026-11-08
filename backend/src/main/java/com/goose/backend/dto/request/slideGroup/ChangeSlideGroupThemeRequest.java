package com.goose.backend.dto.request.slideGroup;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ChangeSlideGroupThemeRequest(
        @NotNull
        @Positive
        Long targetThemeId
) {
}
