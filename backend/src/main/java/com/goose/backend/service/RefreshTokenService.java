package com.goose.backend.service;

import com.goose.backend.model.RefreshToken;
import com.goose.backend.model.User;
import com.goose.backend.repository.TokenRepository;
import com.goose.backend.repository.UserRepository;
import com.goose.backend.role.States;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
