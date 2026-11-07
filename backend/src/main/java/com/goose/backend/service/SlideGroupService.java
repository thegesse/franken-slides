package com.goose.backend.service;

import com.goose.backend.dto.SlideGroupResponse;
import com.goose.backend.dto.ThemeResponse;
import com.goose.backend.model.Slide;
import com.goose.backend.model.SlideGroup;
import com.goose.backend.model.Theme;
import com.goose.backend.model.User;
import com.goose.backend.repository.SlideGroupRepository;
import com.goose.backend.repository.ThemeRepository;
import com.goose.backend.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import java.util.Objects;

public class SlideGroupService {
    private final ThemeRepository themeRepository;
    private final SlideGroupRepository slideGroupRepository;
    private final UserRepository userRepository;

    public SlideGroupService(ThemeRepository themeRepository, SlideGroupRepository slideGroupRepository, UserRepository userRepository) {
        this.themeRepository = themeRepository;
        this.slideGroupRepository = slideGroupRepository;
        this.userRepository = userRepository;
    }

    //helpers
    private SlideGroupResponse toDto(SlideGroup slideGroup) {
        return new SlideGroupResponse(
                slideGroup.getId(),
                slideGroup.getName(),
                slideGroup.getTheme().getId()
        );
    }
    private SlideGroup getOwnedGroup(Long ownerId, Long themeId, Long groupId) throws AccessDeniedException {
        Theme theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new EntityNotFoundException("Theme not found"));
        if (!Objects.equals(theme.getOwner().getId(), ownerId)) {
            throw new AccessDeniedException("You do not own this theme");
        }

        SlideGroup slideGroup = slideGroupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("Group not found"));
        if (!Objects.equals(slideGroup.getTheme().getId(), themeId)) {
            throw new AccessDeniedException("Group does not belong to this theme");
        }

        return slideGroup;
    }


    public Page<SlideGroupResponse> getSlideGroupForUser(Long ownerId, Long themeId, Pageable pageable) throws AccessDeniedException {
        Theme theme = themeRepository.findById(themeId).orElseThrow(() -> new EntityNotFoundException("Theme doesn't exist"));

        if(!theme.getOwner().getId().equals(ownerId)) {
            throw new AccessDeniedException("You cant acces this theme");
        }

        return slideGroupRepository.findByTheme_Id(themeId, pageable)
                .map(this::toDto);
    }

    public SlideGroupResponse getSlideGroupForUserById(Long ownerId, Long themeId, Long groupId) throws AccessDeniedException {
        SlideGroup slideGroup = getOwnedGroup(ownerId, themeId, groupId);
        return toDto(slideGroup);
    }

    public SlideGroupResponse createSlideGroup(Long ownerId, Long themeId, String name) {
        Theme theme = themeRepository.findById(themeId).orElseThrow(() -> new EntityNotFoundException("Theme not found"));
        if (!Objects.equals(theme.getOwner().getId(), ownerId)) {
            throw new AccessDeniedException("You do not own this theme");
        }
        SlideGroup group = new SlideGroup();
        group.setName(name);
        group.setTheme(theme);

        SlideGroup savedGroup = slideGroupRepository.save(group);
        return toDto(savedGroup);
    }

    
}
