package com.goose.backend.controller;

import com.goose.backend.dto.SlideResponse;
import com.goose.backend.dto.request.slide.ChangeSlideContentRequest;
import com.goose.backend.dto.request.slide.ChangeSlideDescriptionRequest;
import com.goose.backend.dto.request.slide.ChangeSlideTitleRequest;
import com.goose.backend.dto.request.slide.CreateSlideRequest;
import com.goose.backend.service.SlideService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/users/{userId}/themes/{themeId}/groups/{groupId}/slides")
public class SlideController {
    private final SlideService slideService;

    public SlideController(SlideService slideService) {
        this.slideService = slideService;
    }

    @GetMapping
    public ResponseEntity<Page<SlideResponse>> getSlidesForGroup(@PathVariable("userId") Long userId, @PathVariable("themeId") Long themeId, @PathVariable("groupId") Long groupId,
            Pageable pageable) {
        Page<SlideResponse> response = slideService.getSlidesForGroup(userId, themeId, groupId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<SlideResponse>> searchSlidesInGroup(@PathVariable("userId") Long userId, @PathVariable("themeId") Long themeId, @PathVariable("groupId") Long groupId, @RequestParam("title") String title, Pageable pageable) {
        Page<SlideResponse> response = slideService.searchSlidesInGroup(
                userId, themeId, groupId, title, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{slideId}")
    public ResponseEntity<SlideResponse> getSlideById(@PathVariable("userId") Long userId, @PathVariable("themeId") Long themeId, @PathVariable("groupId") Long groupId, @PathVariable("slideId") Long slideId) {
        SlideResponse response = slideService.getSlideById(userId, themeId, groupId, slideId);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<SlideResponse> createSlide(@PathVariable("userId") Long userId, @PathVariable("themeId") Long themeId, @PathVariable("groupId") Long groupId, @Valid @RequestBody CreateSlideRequest request) {
        SlideResponse response = slideService.createSlide(
                userId, themeId, groupId, request.title(), request.description(), request.content());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{slideId}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PatchMapping("/{slideId}/title")
    public ResponseEntity<SlideResponse> changeSlideTitle(@PathVariable("userId") Long userId, @PathVariable("themeId") Long themeId, @PathVariable("groupId") Long groupId, @PathVariable("slideId") Long slideId, @Valid @RequestBody ChangeSlideTitleRequest request) {
        SlideResponse response = slideService.changeSlideTitle(userId, themeId, groupId, slideId, request.newTitle());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{slideId}/description")
    public ResponseEntity<SlideResponse> changeSlideDescription(@PathVariable("userId") Long userId, @PathVariable("themeId") Long themeId, @PathVariable("groupId") Long groupId, @PathVariable("slideId") Long slideId, @Valid @RequestBody ChangeSlideDescriptionRequest request) {
        SlideResponse response = slideService.changeSlideDescription(userId, themeId, groupId, slideId, request.newDescription());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{slideId}/content")
    public ResponseEntity<SlideResponse> changeSlideContent(@PathVariable("userId") Long userId, @PathVariable("themeId") Long themeId, @PathVariable("groupId") Long groupId, @PathVariable("slideId") Long slideId, @Valid @RequestBody ChangeSlideContentRequest request) {
        SlideResponse response = slideService.changeSlideContent(userId, themeId, groupId, slideId, request.newContent());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{slideId}")
    public ResponseEntity<Void> deleteSlide(@PathVariable("userId") Long userId, @PathVariable("themeId") Long themeId, @PathVariable("groupId") Long groupId, @PathVariable("slideId") Long slideId) {
        slideService.deleteSlide(userId, themeId, groupId, slideId);
        return ResponseEntity.noContent().build();
    }
}
