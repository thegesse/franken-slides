package com.goose.backend.service;

import com.goose.backend.dto.SlideResponse;
import com.goose.backend.model.Slide;
import com.goose.backend.model.SlideGroup;
import com.goose.backend.repository.SlideGroupRepository;
import com.goose.backend.repository.SlideRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.Objects;

@Service
public class SlideService {
    private final SlideRepository slideRepository;
    private final SlideGroupRepository slideGroupRepository;
    private final JsonMapper jsonMapper;

    public SlideService(SlideRepository slideRepository, SlideGroupRepository slideGroupRepository, JsonMapper jsonMapper) {
        this.slideRepository = slideRepository;
        this.slideGroupRepository = slideGroupRepository;
        this.jsonMapper = jsonMapper;
    }

    //helpers
    private SlideResponse toDto(Slide slide) {
        return new SlideResponse(
                slide.getId(),
                slide.getTitle(),
                slide.getDescription(),
                parseContent(slide.getContent()),
                slide.getGroup().getId()
        );
    }

    private String defaultIfBlank(String value) {
        return value == null || value.isBlank() ? "empty" : value;
    }

    private String serializeContent(JsonNode content) {
        if (content == null || content.isNull() || (content.isObject() && content.isEmpty())) {
            content = jsonMapper.createObjectNode().put("text", "empty");
        }
        if (!content.isObject()) {
            throw new IllegalArgumentException("Slide content must be a JSON object");
        }
        try {
            return jsonMapper.writeValueAsString(content);
        } catch (JacksonException exception) {
            throw new IllegalArgumentException("Slide content could not be serialized", exception);
        }
    }

    private JsonNode parseContent(String content) {
        if (content == null || content.isBlank()) {
            return null;
        }
        try {
            return jsonMapper.readTree(content);
        } catch (JacksonException exception) {
            throw new IllegalStateException("Stored slide content is not valid JSON", exception);
        }
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

    public SlideResponse createSlide(Long ownerId, Long groupId, String title, String desc, JsonNode content) {
        SlideGroup group = slideGroupRepository.findByIdAndTheme_Owner_Id(groupId, ownerId)
                .orElseThrow(() -> new EntityNotFoundException("Group not found"));
        Slide slide = new Slide();
        slide.setTitle(defaultIfBlank(title));
        slide.setDescription(defaultIfBlank(desc));
        slide.setContent(serializeContent(content));
        slide.setGroup(group);

        return toDto(slideRepository.save(slide));
    }
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

    public SlideResponse changeSlideTitle(Long ownerId, Long groupId, Long slideId, String newName) {
        Slide slide = getOwnedSlide(ownerId, groupId, slideId);
        slide.setTitle(defaultIfBlank(newName));
        Slide savedSlide = slideRepository.save(slide);

        return toDto(savedSlide);
    }
    public SlideResponse changeSlideDescription(Long ownerId, Long groupId, Long slideId, String newDesc) {
        Slide slide = getOwnedSlide(ownerId, groupId, slideId);
        slide.setDescription(defaultIfBlank(newDesc));
        Slide savedSlide = slideRepository.save(slide);

        return toDto(savedSlide);
    }

    public SlideResponse changeSlideContent(Long ownerId, Long groupId, Long slideId, JsonNode newContent) {
        Slide slide = getOwnedSlide(ownerId, groupId, slideId);
        slide.setContent(serializeContent(newContent));
        Slide savedSlide = slideRepository.save(slide);

        return toDto(savedSlide);
    }

    public void deleteSlide(Long ownerId, Long groupId, Long slideId) {
        Slide slide = getOwnedSlide(ownerId, groupId, slideId);
        slideRepository.deleteById(slideId);
    }

}
