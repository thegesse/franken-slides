package com.goose.backend.service;

import com.goose.backend.dto.SlideResponse;
import com.goose.backend.model.Slide;
import com.goose.backend.model.SlideGroup;
import com.goose.backend.repository.SlideGroupRepository;
import com.goose.backend.repository.SlideRepository;
import com.goose.backend.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

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
}
