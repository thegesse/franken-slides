package com.goose.backend.service;

import com.goose.backend.dto.UserResponse;
import com.goose.backend.model.User;
import com.goose.backend.repository.UserRepository;
import com.goose.backend.role.Roles;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock UserRepository userRepository;
    @InjectMocks UserService userService;

    @Test
    void getCurrentProfileReturnsPublicUserFields() {
        when(userRepository.findById(3L)).thenReturn(Optional.of(user(3L, Roles.USER)));

        UserResponse response = userService.getCurrentUserProfile(3L);

        assertEquals(new UserResponse(3L, "user3", "user3@example.com"), response);
    }

    @Test
    void adminUserListingRejectsNonAdminActor() {
        when(userRepository.findById(3L)).thenReturn(Optional.of(user(3L, Roles.USER)));

        assertThrows(AccessDeniedException.class,
                () -> userService.findUsersForAdmin(3L, org.springframework.data.domain.Pageable.unpaged()));
        verify(userRepository, never()).findAll(any(org.springframework.data.domain.Pageable.class));
    }

    @Test
    void adminCannotUseAdminDeleteToDeleteTheirOwnAccount() {
        when(userRepository.findById(3L)).thenReturn(Optional.of(user(3L, Roles.ADMIN)));

        assertThrows(IllegalArgumentException.class, () -> userService.deleteUserAsAdmin(3L, 3L));
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void getProfileThrowsWhenUserIsMissing() {
        when(userRepository.findById(3L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.getCurrentUserProfile(3L));
    }

    private User user(Long id, Roles role) {
        User user = new User();
        user.setId(id);
        user.setUsername("user" + id);
        user.setEmail("user" + id + "@example.com");
        user.setRole(role);
        return user;
    }
}
