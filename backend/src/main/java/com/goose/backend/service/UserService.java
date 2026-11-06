package com.goose.backend.service;

import com.goose.backend.dto.UserResponse;
import com.goose.backend.model.User;
import com.goose.backend.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private UserResponse mapToUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail()
        );
    }

    public Page<UserResponse> findUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(this::mapToUserResponse);
    }

    public Page<UserResponse> findUserByUsername(String username,  Pageable pageable) {
        Page<User> users = userRepository.findByUsername(username, pageable);
        if (users.isEmpty()) {
            throw new RuntimeException("No users found with this username");
        }
        return users.map(this::mapToUserResponse);
    }
    public UserResponse findUserByEmail(String email, Pageable pageable) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email doesn't exist"));


        return mapToUserResponse(user);
    }

    public void deleteUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User doesn't exist"));

        userRepository.delete(user);
    }
}
