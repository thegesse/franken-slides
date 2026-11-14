package com.goose.backend.service;

import com.goose.backend.model.RefreshToken;
import com.goose.backend.model.User;
import com.goose.backend.repository.TokenRepository;
import com.goose.backend.repository.UserRepository;
import com.goose.backend.role.States;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class RefreshTokenService {
    private final TokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final Duration refreshTokenLifetime;
    private final SecureRandom secureRandom = new SecureRandom();

    public RefreshTokenService(
            TokenRepository tokenRepository,
            UserRepository userRepository,
            @Value("${security.jwt.refresh-token-lifetime}") Duration refreshTokenLifetime) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.refreshTokenLifetime = refreshTokenLifetime;
    }

    @Transactional
    public String createRefreshToken(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        String rawToken = generateRawToken();
        Instant now = Instant.now();

        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setCreatedAt(now);
        token.setExpiresAt(now.plus(refreshTokenLifetime));
        token.setTokenState(States.VALID);
        token.setTokenHash(hashToken(rawToken));

        tokenRepository.save(token);
        return rawToken;
    }

    private String generateRawToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String rawToken) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    @Transactional
    public RefreshTokenRotation rotateToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw invalidRefreshToken();
        }

        String tokenHash = hashToken(rawToken);
        RefreshToken token = tokenRepository.findByTokenHashForUpdate(tokenHash)
                .orElseThrow(this::invalidRefreshToken);

        Instant now = Instant.now();
        if (token.getTokenState() != States.VALID || !token.getExpiresAt().isAfter(now)) {
            throw invalidRefreshToken();
        }

        token.setTokenState(States.ROTATED);
        String nextRawToken = generateRawToken();
        User user = userRepository.findById(token.getUser().getId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        RefreshToken newToken = new RefreshToken();
        newToken.setUser(user);
        newToken.setCreatedAt(now);
        newToken.setExpiresAt(now.plus(refreshTokenLifetime));
        newToken.setTokenState(States.VALID);
        newToken.setTokenHash(hashToken(nextRawToken));

        tokenRepository.save(token);
        tokenRepository.save(newToken);
        return new RefreshTokenRotation(user, nextRawToken);
    }

    @Transactional
    public void revokeToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return;
        }

        tokenRepository.findByTokenHashForUpdate(hashToken(rawToken))
                .filter(token -> token.getTokenState() == States.VALID)
                .ifPresent(token -> {
                    token.setTokenState(States.REVOKED);
                    tokenRepository.save(token);
                });
    }

    private ResponseStatusException invalidRefreshToken() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired refresh token");
    }
}
