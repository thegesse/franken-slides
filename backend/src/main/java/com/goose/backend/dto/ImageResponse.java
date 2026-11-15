package com.goose.backend.dto;

public record ImageResponse(
        Long id,
        String alt,
        String imagePath
) {
}
