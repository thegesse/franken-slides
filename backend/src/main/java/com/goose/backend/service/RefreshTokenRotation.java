package com.goose.backend.service;

import com.goose.backend.model.User;

public record RefreshTokenRotation(User user, String rawToken) {
}
