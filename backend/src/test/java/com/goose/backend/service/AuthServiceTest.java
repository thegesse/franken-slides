package com.goose.backend.service;

import com.goose.backend.dto.AuthResponse;
import com.goose.backend.model.User;
import com.goose.backend.repository.UserRepository;
import com.goose.backend.role.Roles;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtService jwtService;
    @Mock RefreshTokenService refreshTokenService;
    @InjectMocks AuthService authService;

    @Test
    void registerSavesEncodedPasswordAndReturnsUserDetails() {
        when(userRepository.existsByEmail("a@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(jwtService.createAccessToken(any(User.class))).thenReturn("access-token");
        when(jwtService.getAccessTokenLifetime()).thenReturn(Duration.ofMinutes(5));
        when(refreshTokenService.createRefreshToken(12L)).thenReturn("refresh-token");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(12L);
            return user;
        });

        AuthSession session = authService.register("alice", "a@example.com", "password123");

        assertEquals(new AuthResponse(12L, "alice", "a@example.com", Roles.USER,
                "access-token", "Bearer", 300), session.response());
        assertEquals("refresh-token", session.rawRefreshToken());
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(argThat(user ->
                "encoded-password".equals(user.getPassword()) && user.getRole() == Roles.USER));
    }

    @Test
    void registerRejectsAnEmailAlreadyInUse() {
        when(userRepository.existsByEmail("a@example.com")).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> authService.register("alice", "a@example.com", "password123"));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(userRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void loginReturnsUserWhenPasswordMatches() {
        User user = user(12L, "alice", "a@example.com", "encoded-password", Roles.USER);
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encoded-password")).thenReturn(true);
        when(jwtService.createAccessToken(user)).thenReturn("access-token");
        when(jwtService.getAccessTokenLifetime()).thenReturn(Duration.ofMinutes(5));
        when(refreshTokenService.createRefreshToken(12L)).thenReturn("refresh-token");

        AuthSession session = authService.login("alice", "password123");

        assertEquals(new AuthResponse(12L, "alice", "a@example.com", Roles.USER,
                "access-token", "Bearer", 300), session.response());
        assertEquals("refresh-token", session.rawRefreshToken());
    }

    @Test
    void loginRejectsIncorrectPassword() {
        User user = user(12L, "alice", "a@example.com", "encoded-password", Roles.USER);
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encoded-password")).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> authService.login("alice", "wrong"));
    }

    private User user(Long id, String username, String email, String password, Roles role) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        user.setRole(role);
        return user;
    }
}
