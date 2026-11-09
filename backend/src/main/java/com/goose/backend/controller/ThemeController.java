package com.goose.backend.controller;

import com.goose.backend.dto.ThemeResponse;
import com.goose.backend.dto.request.theme.CreateThemeRequest;
import com.goose.backend.dto.request.theme.UpdateThemeRequest;
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

    public ThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @GetMapping
    public ResponseEntity<Page<ThemeResponse>> getThemesForUser(@PathVariable("userId") Long userId, Pageable pageable) {
        Page<ThemeResponse> response = themeService.findAllThemesForUser(userId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{themeId}")
    public ResponseEntity<ThemeResponse> getThemeById(@PathVariable("userId") Long userId, @PathVariable("themeId") Long themeId) {
        ThemeResponse response = themeService.findThemeByIdForUser(userId, themeId);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ThemeResponse> createTheme(@PathVariable("userId") Long userId, @Valid @RequestBody CreateThemeRequest request) {
        ThemeResponse response = themeService.createThemeForUser(userId, request.title());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{themeId}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PatchMapping("/{themeId}")
    public ResponseEntity<ThemeResponse> changeThemeName(@PathVariable("userId") Long userId, @PathVariable("themeId") Long themeId, @Valid @RequestBody UpdateThemeRequest request) {
        ThemeResponse response = themeService.updateThemeForUser(userId, themeId, request.newTitle());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{themeId}")
    public ResponseEntity<Void> deleteTheme(@PathVariable("userId") Long userId, @PathVariable("themeId") Long themeId) {
        themeService.deleteThemeForUser(userId, themeId);
        return ResponseEntity.noContent().build();
    }
}
