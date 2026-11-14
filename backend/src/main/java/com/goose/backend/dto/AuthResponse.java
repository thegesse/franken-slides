package com.goose.backend.dto;

import com.goose.backend.role.Roles;

import javax.management.relation.Role;

public record AuthResponse(
        Long id,
        String username,
        String email,
        Roles role,
        String accessToken,
        String tokenType,
        long expiresIn
) {
}
