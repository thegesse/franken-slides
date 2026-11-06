package com.goose.backend.service;

import com.goose.backend.dto.AuthResponse;
import com.goose.backend.model.User;
import com.goose.backend.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private AuthResponse login(String username, String password) {
        User user = (User) userRepository.findByUsername(username).orElseThrow();
        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("Incorrect password");
        }

        return new AuthResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole()
        );
    }
}
