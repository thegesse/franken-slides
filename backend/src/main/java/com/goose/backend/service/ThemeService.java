package com.goose.backend.service;

import com.goose.backend.dto.ThemeResponse;
import com.goose.backend.model.Theme;
import com.goose.backend.model.User;
import com.goose.backend.repository.ThemeRepository;
import com.goose.backend.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ThemeService {
    private final ThemeRepository themeRepository;
    private final UserRepository userRepository;

    public ThemeService(ThemeRepository themeRepository, UserRepository userRepository) {
        this.themeRepository = themeRepository;
        this.userRepository = userRepository;
    }
    //helpers
    private ThemeResponse mapToThemeResponse(Theme theme) {
        return new ThemeResponse(
                theme.getId(),
                theme.getName(),
                theme.getOwner().getId()
        );
    }
    private boolean isExistAndIsOwned(Long ownerId, Long themeId) {
        Theme theme = themeRepository.findById(themeId).orElse(return false);
        if(!theme.getOwner().getId().equals(ownerId)) {
            return false;
        }
        return true;
    }

    public Page<ThemeResponse> findAllThemesForUser(Long userId, Pageable pageable) {
        return themeRepository.findByOwnerId(userId, pageable)
                .map(this::mapToThemeResponse);
    }
    //TODO find theme by id, find theme by name, remember to have ownership checks for all those

    public ThemeResponse findThemeByIdForUser(Long ownerId, Long themeId) {
        Theme theme = themeRepository.findById(themeId).orElseThrow();
        if(!theme.getOwner().getId().equals(ownerId)) {
            throw new RuntimeException("You cant access this theme");
        }
        return mapToThemeResponse(theme);
    }

    public ThemeResponse createThemeForUser(Long ownerId, String name) {
        User owner = userRepository.findById(ownerId).orElseThrow();

        Theme theme = new Theme();
        theme.setName(name);
        theme.setOwner(owner);

        Theme savedTheme = themeRepository.save(theme);
        return mapToThemeResponse(savedTheme);
    }

    public ThemeResponse updateThemeForUser(Long ownerId, Long themeId, String newName) {

    }
}
