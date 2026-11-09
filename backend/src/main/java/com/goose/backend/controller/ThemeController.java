package com.goose.backend.controller;

import com.goose.backend.dto.ThemeResponse;
import com.goose.backend.dto.request.theme.CreateThemeRequest;
import com.goose.backend.dto.request.theme.UpdateThemeRequest;
import com.goose.backend.service.ThemeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.http.HttpResponse;
import java.nio.file.AccessDeniedException;

@RestController
@RequestMapping("/api/v1/themes")
public class ThemeController {
    private final ThemeService service;

    public ThemeController(ThemeService service) {
        this.service = service;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Page<ThemeResponse>> getThemesForUser(@PathVariable Long userId, Pageable pageable) {
        Page<ThemeResponse> response = service.findAllThemesForUser(userId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}/{themeId}")
    public ResponseEntity<ThemeResponse> getThemesById(@PathVariable Long userId, @PathVariable Long themeId) throws AccessDeniedException {
        ThemeResponse response = service.findThemeByIdForUser(userId, themeId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{userId}")
    public ResponseEntity<ThemeResponse> createTheme(@PathVariable Long userId, @Valid @RequestBody CreateThemeRequest request) {
        ThemeResponse response = service.createThemeForUser(userId, request.title());

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{userId}/{themeId}")
    public ResponseEntity<ThemeResponse> changeThemeName(@PathVariable Long userId, @PathVariable Long themeId, @Valid @RequestBody UpdateThemeRequest request) throws AccessDeniedException {
        ThemeResponse response = service.updateThemeForUser(userId, themeId, request.newTitle());

        return ResponseEntity.ok(response);
    }
    @DeleteMapping("/{userId}/{themeId}")
    public ResponseEntity<Void> deleteTheme(@PathVariable Long userId, @PathVariable Long themeId) throws AccessDeniedException {
        service.deleteThemeForUser(userId, themeId);
        return ResponseEntity.noContent().build();
    }

}
