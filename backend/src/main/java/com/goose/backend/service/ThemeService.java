package com.goose.backend.service;

import com.goose.backend.dto.ThemeResponse;
import com.goose.backend.model.Theme;
import com.goose.backend.repository.ThemeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ThemeService {
    private final ThemeRepository themeRepository;

    public ThemeService(ThemeRepository themeRepository) {
        this.themeRepository = themeRepository;
    }

    private ThemeResponse mapToThemeResponse(Theme theme) {
        return new ThemeResponse(
                theme.getId(),
                theme.getName(),
                theme.getOwner().getId()
        );
    }

    public Page<ThemeResponse> findAllThemesForUser(Long userId, Pageable pageable) {
        return themeRepository.findByOwnerId(userId, pageable)
                .map(this::mapToThemeResponse);
    }
    //TODO find theme by id, find theme by name, remember to have ownership checks for all those
}
