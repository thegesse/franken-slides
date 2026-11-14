package com.goose.backend.service;

import com.goose.backend.dto.ThemeResponse;
import com.goose.backend.model.Theme;
import com.goose.backend.model.User;
import com.goose.backend.repository.ThemeRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ThemeServiceTest {
    @Mock ThemeRepository themeRepository;
    @Mock UserRepository userRepository;
    @InjectMocks ThemeService themeService;

    @Test
    void createThemeAssignsOwnerAndReturnsResponse() {
        User owner = user(3L);
        when(userRepository.findById(3L)).thenReturn(Optional.of(owner));
        when(themeRepository.save(any(Theme.class))).thenAnswer(invocation -> {
            Theme theme = invocation.getArgument(0);
            theme.setId(20L);
            return theme;
        });

        ThemeResponse response = themeService.createThemeForUser(3L, "Lecture");

        assertEquals(new ThemeResponse(20L, "Lecture", 3L), response);
        verify(themeRepository).save(argThat(theme ->
                "Lecture".equals(theme.getName()) && theme.getOwner() == owner));
    }

    @Test
    void updateThemeChangesNameForItsOwner() {
        Theme theme = theme(20L, "Old", user(3L));
        when(themeRepository.findById(20L)).thenReturn(Optional.of(theme));
        when(themeRepository.save(theme)).thenReturn(theme);

        ThemeResponse response = themeService.updateThemeForUser(3L, 20L, "New");

        assertEquals(new ThemeResponse(20L, "New", 3L), response);
        verify(themeRepository).save(theme);
    }

    @Test
    void updateThemeRejectsAnotherUsersTheme() {
        when(themeRepository.findById(20L)).thenReturn(Optional.of(theme(20L, "Private", user(4L))));

        assertThrows(AccessDeniedException.class,
                () -> themeService.updateThemeForUser(3L, 20L, "Changed"));
        verify(themeRepository, never()).save(any());
    }

    @Test
    void getThemeThrowsWhenThemeDoesNotExist() {
        when(themeRepository.findById(20L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> themeService.findThemeByIdForUser(3L, 20L));
    }

    private User user(Long id) {
        User user = new User();
        user.setId(id);
        user.setRole(Roles.USER);
        return user;
    }

    private Theme theme(Long id, String name, User owner) {
        Theme theme = new Theme();
        theme.setId(id);
        theme.setName(name);
        theme.setOwner(owner);
        return theme;
    }
}
