package com.goose.backend.controller;

import com.goose.backend.dto.ThemeResponse;
import com.goose.backend.dto.request.theme.CreateThemeRequest;
import com.goose.backend.dto.request.theme.UpdateThemeRequest;
import com.goose.backend.security.AuthenticatedUser;
import com.goose.backend.service.ThemeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/users/{userId}/themes")
public class ThemeController {
    private final ThemeService themeService;
    private final AuthenticatedUser authenticatedUser;

    public ThemeController(ThemeService themeService, AuthenticatedUser authenticatedUser) {
        this.themeService = themeService;
        this.authenticatedUser = authenticatedUser;
    }

    @GetMapping
    public ResponseEntity<Page<ThemeResponse>> getThemesForUser(@PathVariable("userId") Long userId, Pageable pageable) {
        Long ownerId = authenticatedUser.requirePathUserId(userId);
        Page<ThemeResponse> response = themeService.findAllThemesForUser(ownerId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{themeId}")
    public ResponseEntity<ThemeResponse> getThemeById(@PathVariable("userId") Long userId, @PathVariable("themeId") Long themeId) {
        Long ownerId = authenticatedUser.requirePathUserId(userId);
        ThemeResponse response = themeService.findThemeByIdForUser(ownerId, themeId);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ThemeResponse> createTheme(@PathVariable("userId") Long userId, @Valid @RequestBody CreateThemeRequest request) {
        Long ownerId = authenticatedUser.requirePathUserId(userId);
        ThemeResponse response = themeService.createThemeForUser(ownerId, request.title());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{themeId}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PatchMapping("/{themeId}")
    public ResponseEntity<ThemeResponse> changeThemeName(@PathVariable("userId") Long userId, @PathVariable("themeId") Long themeId, @Valid @RequestBody UpdateThemeRequest request) {
        Long ownerId = authenticatedUser.requirePathUserId(userId);
        ThemeResponse response = themeService.updateThemeForUser(ownerId, themeId, request.newTitle());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{themeId}")
    public ResponseEntity<Void> deleteTheme(@PathVariable("userId") Long userId, @PathVariable("themeId") Long themeId) {
        Long ownerId = authenticatedUser.requirePathUserId(userId);
        themeService.deleteThemeForUser(ownerId, themeId);
        return ResponseEntity.noContent().build();
    }
}
