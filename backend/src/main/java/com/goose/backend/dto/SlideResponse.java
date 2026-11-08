package com.goose.backend.dto;

public record SlideResponse(
        Long id,
        String title,
        String description,
        String content,
        Long groupId
) {
}
