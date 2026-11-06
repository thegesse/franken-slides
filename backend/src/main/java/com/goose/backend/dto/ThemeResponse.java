package com.goose.backend.dto;

public record ThemeResponse(
        Long id,
        String name,
        Long owner_id
) {
}
