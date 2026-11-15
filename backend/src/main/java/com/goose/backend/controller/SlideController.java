package com.goose.backend.controller;

import com.goose.backend.dto.SlideResponse;
import com.goose.backend.dto.ImageResponse;
import com.goose.backend.dto.request.slide.ChangeSlideContentRequest;
import com.goose.backend.dto.request.slide.ChangeSlideDescriptionRequest;
import com.goose.backend.dto.request.slide.ChangeSlideTitleRequest;
import com.goose.backend.dto.request.slide.CreateSlideRequest;
import com.goose.backend.security.AuthenticatedUser;
import com.goose.backend.service.StoredImage;
import com.goose.backend.service.SlideService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/users/{userId}/themes/{themeId}/groups/{groupId}/slides")
public class SlideController {
    private final SlideService slideService;
    private final AuthenticatedUser authenticatedUser;

    public SlideController(SlideService slideService, AuthenticatedUser authenticatedUser) {
        this.slideService = slideService;
        this.authenticatedUser = authenticatedUser;
    }

    @GetMapping
    public ResponseEntity<Page<SlideResponse>> getSlidesForGroup(@PathVariable("userId") Long userId, @PathVariable("themeId") Long themeId, @PathVariable("groupId") Long groupId,
            Pageable pageable) {
        Long ownerId = authenticatedUser.requirePathUserId(userId);
        Page<SlideResponse> response = slideService.getSlidesForGroup(ownerId, themeId, groupId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<SlideResponse>> searchSlidesInGroup(@PathVariable("userId") Long userId, @PathVariable("themeId") Long themeId, @PathVariable("groupId") Long groupId, @RequestParam("title") String title, Pageable pageable) {
        Long ownerId = authenticatedUser.requirePathUserId(userId);
        Page<SlideResponse> response = slideService.searchSlidesInGroup(
                ownerId, themeId, groupId, title, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{slideId}")
    public ResponseEntity<SlideResponse> getSlideById(@PathVariable("userId") Long userId, @PathVariable("themeId") Long themeId, @PathVariable("groupId") Long groupId, @PathVariable("slideId") Long slideId) {
        Long ownerId = authenticatedUser.requirePathUserId(userId);
        SlideResponse response = slideService.getSlideById(ownerId, themeId, groupId, slideId);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<SlideResponse> createSlide(@PathVariable("userId") Long userId, @PathVariable("themeId") Long themeId, @PathVariable("groupId") Long groupId, @Valid @RequestBody CreateSlideRequest request) {
        Long ownerId = authenticatedUser.requirePathUserId(userId);
        SlideResponse response = slideService.createSlide(
                ownerId, themeId, groupId, request.title(), request.description(), request.content());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{slideId}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PatchMapping("/{slideId}/title")
    public ResponseEntity<SlideResponse> changeSlideTitle(@PathVariable("userId") Long userId, @PathVariable("themeId") Long themeId, @PathVariable("groupId") Long groupId, @PathVariable("slideId") Long slideId, @Valid @RequestBody ChangeSlideTitleRequest request) {
        Long ownerId = authenticatedUser.requirePathUserId(userId);
        SlideResponse response = slideService.changeSlideTitle(ownerId, themeId, groupId, slideId, request.newTitle());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{slideId}/description")
    public ResponseEntity<SlideResponse> changeSlideDescription(@PathVariable("userId") Long userId, @PathVariable("themeId") Long themeId, @PathVariable("groupId") Long groupId, @PathVariable("slideId") Long slideId, @Valid @RequestBody ChangeSlideDescriptionRequest request) {
        Long ownerId = authenticatedUser.requirePathUserId(userId);
        SlideResponse response = slideService.changeSlideDescription(ownerId, themeId, groupId, slideId, request.newDescription());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{slideId}/content")
    public ResponseEntity<SlideResponse> changeSlideContent(@PathVariable("userId") Long userId, @PathVariable("themeId") Long themeId, @PathVariable("groupId") Long groupId, @PathVariable("slideId") Long slideId, @Valid @RequestBody ChangeSlideContentRequest request) {
        Long ownerId = authenticatedUser.requirePathUserId(userId);
        SlideResponse response = slideService.changeSlideContent(ownerId, themeId, groupId, slideId, request.newContent());
        return ResponseEntity.ok(response);
    }

    @PutMapping(value = "/{slideId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageResponse> addOrReplaceImage(
            @PathVariable("userId") Long userId,
            @PathVariable("themeId") Long themeId,
            @PathVariable("groupId") Long groupId,
            @PathVariable("slideId") Long slideId,
            @RequestParam(value = "alt", required = false) String alt,
            @RequestPart("file") MultipartFile file) {
        Long ownerId = authenticatedUser.requirePathUserId(userId);
        ImageResponse response = slideService.addImageToSlide(ownerId, themeId, groupId, slideId, alt, file);
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/{slideId}/image")
    public ResponseEntity<Resource> getSlideImage(
            @PathVariable("userId") Long userId,
            @PathVariable("themeId") Long themeId,
            @PathVariable("groupId") Long groupId,
            @PathVariable("slideId") Long slideId) {
        Long ownerId = authenticatedUser.requirePathUserId(userId);
        StoredImage image = slideService.getImageForSlide(ownerId, themeId, groupId, slideId);
        return ResponseEntity.ok()
                .contentType(image.contentType())
                .body(image.resource());
    }

    @DeleteMapping("/{slideId}/image")
    public ResponseEntity<Void> removeSlideImage(
            @PathVariable("userId") Long userId,
            @PathVariable("themeId") Long themeId,
            @PathVariable("groupId") Long groupId,
            @PathVariable("slideId") Long slideId) {
        Long ownerId = authenticatedUser.requirePathUserId(userId);
        slideService.removeImageFromSlide(ownerId, themeId, groupId, slideId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{slideId}")
    public ResponseEntity<Void> deleteSlide(@PathVariable("userId") Long userId, @PathVariable("themeId") Long themeId, @PathVariable("groupId") Long groupId, @PathVariable("slideId") Long slideId) {
        Long ownerId = authenticatedUser.requirePathUserId(userId);
        slideService.deleteSlide(ownerId, themeId, groupId, slideId);
        return ResponseEntity.noContent().build();
    }
}
