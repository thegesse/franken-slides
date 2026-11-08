package com.goose.backend.service;

import com.goose.backend.dto.SlideResponse;
import com.goose.backend.model.Slide;
import com.goose.backend.repository.SlideGroupRepository;
import com.goose.backend.repository.SlideRepository;
import com.goose.backend.repository.UserRepository;
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
        SlideGroup group = slideGroupRepository.
    }
}
