package com.goose.backend.service;

import com.goose.backend.dto.SlideResponse;
import com.goose.backend.model.Slide;
import com.goose.backend.model.SlideGroup;
import com.goose.backend.repository.SlideGroupRepository;
import com.goose.backend.repository.SlideRepository;
import com.goose.backend.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class SlideService {
    private final SlideRepository slideRepository;
    private final SlideGroupRepository slideGroupRepository;
    private final UserRepository userRepository;

    public SlideService(SlideRepository slideRepository, SlideGroupRepository slideGroupRepository, UserRepository userRepository) {
        this.slideRepository = slideRepository;
        this.slideGroupRepository = slideGroupRepository;
        this.userRepository = userRepository;
    }

    //helpers
    private SlideResponse toDto(Slide slide) {
        return new SlideResponse(
                slide.getId(),
                slide.getTitle(),
                slide.getDescription(),
                slide.getContent(),
                slide.getGroup().getId()
        );
    }
    private Slide getOwnedSlide(Long ownerId, Long groupId, Long slideId) {
        SlideGroup slideGroup = slideGroupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("Group not found"));
        if(!Objects.equals(slideGroup.getTheme().getOwner().getId(), ownerId)) {
            throw new AccessDeniedException("You do not own this group");
        }

        Slide slide = slideRepository.findById(slideId)
                .orElseThrow(() -> new EntityNotFoundException("Slide not found"));
        if (!Objects.equals(slide.getGroup().getId(), groupId)) {
            throw new EntityNotFoundException("Slide is not in this group");
        }

        return slide;
    }

    public SlideResponse createSlide(Long ownerId, Long groupId, String title, String desc, String content) {
        SlideGroup group = slideGroupRepository.findByIdAndTheme_Owner_Id(groupId, ownerId)
                .orElseThrow(() -> new EntityNotFoundException("Group not found"));
        if(title == null || title.isBlank()) {
            throw new IllegalArgumentException("Slide title is required");
        }
        Slide slide = new Slide();
        slide.setTitle(title);
        slide.setDescription(desc);
        slide.setContent(content);
        slide.setGroup(group);

        return toDto(slideRepository.save(slide));
    }
    //TODO get slide by id and title, also implement ownership test
    public Page<SlideResponse> getSlidesForGroup(Long ownerId, Long groupId, Pageable pageable){
        slideGroupRepository.findByIdAndTheme_Owner_Id(groupId, ownerId)
                .orElseThrow(() -> new EntityNotFoundException("Group not found"));
        return slideRepository
                .findByGroup_IdOrderByIdAsc(groupId, pageable)
                .map(this::toDto);
    }

    public SlideResponse getSlideById(Long ownerId, Long groupId, Long slideId) {
        Slide slide = getOwnedSlide(ownerId, groupId, slideId);

        return toDto(slide);
    }

    public Page<SlideResponse> getSlideByTitle(Long ownerId, String title, Pageable pageable) {
        return slideRepository.findByGroup_Theme_Owner_IdAndTitleIgnoreCaseOrderByIdAsc(ownerId, title, pageable)
                .map(this::toDto);
    }

}
