package com.goose.backend.service;

import com.goose.backend.dto.AuthResponse;
import com.goose.backend.model.User;
import com.goose.backend.repository.UserRepository;
import com.goose.backend.role.Roles;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       JwtService jwtService, RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    public AuthSession login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("Invalid username or password");
        }

        return createSession(user);
    }

    @Transactional
    public AuthSession register(String username, String email, String password) {
        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(Roles.USER);
        return createSession(userRepository.save(user));
    }

    @Transactional
    public AuthSession refresh(String rawRefreshToken) {
        RefreshTokenRotation rotation = refreshTokenService.rotateToken(rawRefreshToken);
        return new AuthSession(toResponse(rotation.user()), rotation.rawToken());
    }

    public void logout(String rawRefreshToken) {
        refreshTokenService.revokeToken(rawRefreshToken);
    }

    private AuthSession createSession(User user) {
        String accessToken = jwtService.createAccessToken(user);
        String rawRefreshToken = refreshTokenService.createRefreshToken(user.getId());
        AuthResponse response = new AuthResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                accessToken,
                "Bearer",
                jwtService.getAccessTokenLifetime().toSeconds()
        );
        return new AuthSession(response, rawRefreshToken);
    }

    private AuthResponse toResponse(User user) {
        String accessToken = jwtService.createAccessToken(user);
        return new AuthResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                accessToken,
                "Bearer",
                jwtService.getAccessTokenLifetime().toSeconds()
        );
    }
}
