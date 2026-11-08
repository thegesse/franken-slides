package com.goose.backend.service;

import com.goose.backend.dto.UserResponse;
import com.goose.backend.model.User;
import com.goose.backend.repository.UserRepository;
import com.goose.backend.role.Roles;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

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

    private User requireUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    private void requireAdmin(Long actorId) {
        User actor = requireUser(actorId);
        if (actor.getRole() != Roles.ADMIN) {
            throw new AccessDeniedException("Admin role required");
        }
    }

    public UserResponse getCurrentUserProfile(Long actorId) {
        return mapToUserResponse(requireUser(actorId));
    }

    public Page<UserResponse> findUsersForAdmin(Long actorId, Pageable pageable) {
        requireAdmin(actorId);
        return userRepository.findAll(pageable)
                .map(this::mapToUserResponse);
    }

    public Page<UserResponse> findUsersByUsernameForAdmin(Long actorId, String username, Pageable pageable) {
        requireAdmin(actorId);
        return userRepository.findByUsername(username, pageable)
                .map(this::mapToUserResponse);
    }

    public UserResponse findUserByEmailForAdmin(Long actorId, String email) {
        requireAdmin(actorId);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return mapToUserResponse(user);
    }

    @Transactional
    public void deleteOwnAccount(Long actorId) {
        userRepository.delete(requireUser(actorId));
    }

    @Transactional
    public void deleteUserAsAdmin(Long actorId, Long targetUserId) {
        requireAdmin(actorId);
        if (Objects.equals(actorId, targetUserId)) {
            throw new IllegalArgumentException("Use deleteOwnAccount to delete your own account");
        }
        userRepository.delete(requireUser(targetUserId));
    }
}
