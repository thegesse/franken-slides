package com.goose.backend.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class AuthenticatedUser {
    public Long requirePathUserId(Long requestedUserId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new AccessDeniedException("A valid access token is required");
        }

        final Long authenticatedUserId;
        try {
            authenticatedUserId = Long.valueOf(jwt.getSubject());
        } catch (NumberFormatException | NullPointerException exception) {
            throw new AccessDeniedException("The access token does not contain a valid user ID");
        }

        if (!Objects.equals(authenticatedUserId, requestedUserId)) {
            throw new AccessDeniedException("You cannot access another user's resources");
        }
        return authenticatedUserId;
    }
}
