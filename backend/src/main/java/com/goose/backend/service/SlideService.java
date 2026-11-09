package com.goose.backend.service;

import com.goose.backend.dto.SlideResponse;
import com.goose.backend.mapper.SlideContentJsonConverter;
import com.goose.backend.model.Slide;
import com.goose.backend.model.SlideGroup;
import com.goose.backend.repository.SlideGroupRepository;
import com.goose.backend.repository.SlideRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;

import java.util.Objects;

@Service
public class SlideService {
    private final SlideRepository slideRepository;
    private final SlideGroupRepository slideGroupRepository;
    private final SlideContentJsonConverter contentJsonConverter;

    public SlideService(SlideRepository slideRepository, SlideGroupRepository slideGroupRepository,
                        SlideContentJsonConverter contentJsonConverter) {
        this.slideRepository = slideRepository;
        this.slideGroupRepository = slideGroupRepository;
        this.contentJsonConverter = contentJsonConverter;
    }

    private SlideResponse toDto(Slide slide) {
        return new SlideResponse(
                slide.getId(),
                slide.getTitle(),
                slide.getDescription(),
                contentJsonConverter.parse(slide.getContent()),
                slide.getGroup().getId()
        );
    }

    private String defaultIfBlank(String value) {
        return value == null || value.isBlank() ? "empty" : value;
    }

    private SlideGroup getOwnedGroup(Long ownerId, Long themeId, Long groupId) {
        return slideGroupRepository.findByIdAndTheme_IdAndTheme_Owner_Id(groupId, themeId, ownerId)
                .orElseThrow(() -> new EntityNotFoundException("Group not found in this theme"));
    }

    private Slide getOwnedSlide(Long ownerId, Long themeId, Long groupId, Long slideId) {
        getOwnedGroup(ownerId, themeId, groupId);

        Slide slide = slideRepository.findById(slideId)
                .orElseThrow(() -> new EntityNotFoundException("Slide not found"));
        if (!Objects.equals(slide.getGroup().getId(), groupId)) {
            throw new EntityNotFoundException("Slide not found in this group");
        }
        return slide;
    }

    @Transactional
    public SlideResponse createSlide(Long ownerId, Long themeId, Long groupId,
                                     String title, String description, JsonNode content) {
        SlideGroup group = getOwnedGroup(ownerId, themeId, groupId);
        Slide slide = new Slide();
        slide.setTitle(defaultIfBlank(title));
        slide.setDescription(defaultIfBlank(description));
        slide.setContent(contentJsonConverter.serialize(content));
        slide.setGroup(group);
        return toDto(slideRepository.save(slide));
    }

    public Page<SlideResponse> getSlidesForGroup(Long ownerId, Long themeId, Long groupId, Pageable pageable) {
        getOwnedGroup(ownerId, themeId, groupId);
        return slideRepository.findByGroup_IdOrderByIdAsc(groupId, pageable)
                .map(this::toDto);
    }

    public Page<SlideResponse> searchSlidesInGroup(Long ownerId, Long themeId, Long groupId,
                                                   String title, Pageable pageable) {
        getOwnedGroup(ownerId, themeId, groupId);
        return slideRepository.findByGroup_IdAndTitleContainingIgnoreCaseOrderByIdAsc(groupId, title, pageable)
                .map(this::toDto);
    }

    public SlideResponse getSlideById(Long ownerId, Long themeId, Long groupId, Long slideId) {
        return toDto(getOwnedSlide(ownerId, themeId, groupId, slideId));
    }

    @Transactional
    public SlideResponse changeSlideTitle(Long ownerId, Long themeId, Long groupId, Long slideId, String newTitle) {
        Slide slide = getOwnedSlide(ownerId, themeId, groupId, slideId);
        slide.setTitle(defaultIfBlank(newTitle));
        return toDto(slideRepository.save(slide));
    }

    @Transactional
    public SlideResponse changeSlideDescription(Long ownerId, Long themeId, Long groupId, Long slideId, String newDescription) {
        Slide slide = getOwnedSlide(ownerId, themeId, groupId, slideId);
        slide.setDescription(defaultIfBlank(newDescription));
        return toDto(slideRepository.save(slide));
    }

    @Transactional
    public SlideResponse changeSlideContent(Long ownerId, Long themeId, Long groupId, Long slideId, JsonNode newContent) {
        Slide slide = getOwnedSlide(ownerId, themeId, groupId, slideId);
        slide.setContent(contentJsonConverter.serialize(newContent));
        return toDto(slideRepository.save(slide));
    }

    @Transactional
    public void deleteSlide(Long ownerId, Long themeId, Long groupId, Long slideId) {
        Slide slide = getOwnedSlide(ownerId, themeId, groupId, slideId);
        slideRepository.delete(slide);
    }
}
