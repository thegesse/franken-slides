package com.goose.backend.service;

import com.goose.backend.dto.AuthResponse;

/** Internal login result; the refresh token is sent only as an HttpOnly cookie. */
public record AuthSession(AuthResponse response, String rawRefreshToken) {
}
