package com.goose.backend.dto;

import tools.jackson.databind.JsonNode;

public record SlideResponse(
        Long id,
        String title,
        String description,
        JsonNode content,
        Long groupId
) {
}
