package com.goose.backend.service;

import com.goose.backend.dto.SlideGroupResponse;
import com.goose.backend.model.SlideGroup;
import com.goose.backend.model.Theme;
import com.goose.backend.model.User;
import com.goose.backend.repository.SlideGroupRepository;
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
class SlideGroupServiceTest {
    @Mock ThemeRepository themeRepository;
    @Mock SlideGroupRepository slideGroupRepository;
    @Mock UserRepository userRepository;
    @InjectMocks SlideGroupService slideGroupService;

    @Test
    void createGroupAssociatesItWithOwnedTheme() {
        Theme theme = theme(7L, user(3L));
        when(themeRepository.findById(7L)).thenReturn(Optional.of(theme));
        when(slideGroupRepository.save(any(SlideGroup.class))).thenAnswer(invocation -> {
            SlideGroup group = invocation.getArgument(0);
            group.setId(30L);
            return group;
        });

        SlideGroupResponse response = slideGroupService.createSlideGroup(3L, 7L, "Intro");

        assertEquals(new SlideGroupResponse(30L, "Intro", 7L), response);
        verify(slideGroupRepository).save(argThat(group ->
                "Intro".equals(group.getName()) && group.getTheme() == theme));
    }

    @Test
    void renameGroupRejectsGroupOutsideRequestedTheme() {
        when(themeRepository.findById(7L)).thenReturn(Optional.of(theme(7L, user(3L))));
        when(slideGroupRepository.findById(30L)).thenReturn(Optional.of(group(30L, theme(8L, user(3L)))));

        assertThrows(AccessDeniedException.class,
                () -> slideGroupService.changeSlideGroupName(3L, 7L, 30L, "Renamed"));
        verify(slideGroupRepository, never()).save(any());
    }

    @Test
    void moveGroupToAnotherThemeOwnedBySameUser() {
        Theme currentTheme = theme(7L, user(3L));
        Theme targetTheme = theme(8L, user(3L));
        SlideGroup group = group(30L, currentTheme);
        when(themeRepository.findById(7L)).thenReturn(Optional.of(currentTheme));
        when(slideGroupRepository.findById(30L)).thenReturn(Optional.of(group));
        when(themeRepository.findById(8L)).thenReturn(Optional.of(targetTheme));
        when(slideGroupRepository.save(group)).thenReturn(group);

        SlideGroupResponse response = slideGroupService.changeSlideGroupTheme(3L, 7L, 30L, 8L);

        assertEquals(new SlideGroupResponse(30L, "Group", 8L), response);
        assertSame(targetTheme, group.getTheme());
    }

    @Test
    void createGroupThrowsWhenThemeIsMissing() {
        when(themeRepository.findById(7L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> slideGroupService.createSlideGroup(3L, 7L, "Intro"));
        verifyNoInteractions(slideGroupRepository);
    }

    private User user(Long id) {
        User user = new User();
        user.setId(id);
        user.setRole(Roles.USER);
        return user;
    }

    private Theme theme(Long id, User owner) {
        Theme theme = new Theme();
        theme.setId(id);
        theme.setOwner(owner);
        return theme;
    }

    private SlideGroup group(Long id, Theme theme) {
        SlideGroup group = new SlideGroup();
        group.setId(id);
        group.setName("Group");
        group.setTheme(theme);
        return group;
    }
}
