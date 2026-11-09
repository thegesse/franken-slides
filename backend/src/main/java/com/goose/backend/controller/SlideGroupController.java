package com.goose.backend.controller;

import com.goose.backend.dto.SlideGroupResponse;
import com.goose.backend.dto.request.slideGroup.ChangeSlideGroupName;
import com.goose.backend.dto.request.slideGroup.ChangeSlideGroupThemeRequest;
import com.goose.backend.dto.request.slideGroup.CreateSlideGroupRequest;
import com.goose.backend.service.SlideGroupService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/themes") //TODO correct the endpoints
public class SlideGroupController {
    private final SlideGroupService service;

    public SlideGroupController(SlideGroupService service) {
        this.service = service;
    }

    @GetMapping("/{userId}/{themeId}/groups")
    public ResponseEntity<Page<SlideGroupResponse>> getGroupsFromThemes(@PathVariable Long userId, @PathVariable Long themeId, Pageable pageable) {
        Page<SlideGroupResponse> response = service.getSlideGroupForUser(userId, themeId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}/{themeId}/groups/{groupId}")
    public ResponseEntity<SlideGroupResponse> getGroupById(@PathVariable Long userId, @PathVariable Long themeId, @PathVariable Long groupId) {
        SlideGroupResponse response = service.getSlideGroupForUserById(userId, themeId, groupId);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/{userId}/{themeId}/groups")
    public ResponseEntity<SlideGroupResponse> createGroup(@PathVariable Long userId, @PathVariable Long themeId, @Valid @RequestBody CreateSlideGroupRequest request) {
        SlideGroupResponse response = service.createSlideGroup(userId, themeId, request.name());
        return ResponseEntity.ok(response);
    }
    @PatchMapping("/{userId}/{themeId}/groups/{groupId}")
    public ResponseEntity<SlideGroupResponse> changeGroupName(@PathVariable Long userId, @PathVariable Long themeId, @PathVariable Long groupId, @Valid @RequestBody ChangeSlideGroupName request) {
        SlideGroupResponse response = service.changeSlideGroupName(userId, themeId, groupId, request.name());
        return ResponseEntity.ok(response);
    }
    @PutMapping("/{userId}/{themeId}/groups/{groupId}")
    public ResponseEntity<SlideGroupResponse> changeGroupTheme(@PathVariable Long userId, @PathVariable Long themeId, @PathVariable Long groupId, @Valid @RequestBody ChangeSlideGroupThemeRequest request) {
        SlideGroupResponse response = service.changeSlideGroupTheme(userId, themeId, groupId, request.targetThemeId());
        return ResponseEntity.ok(response);
    }
    @DeleteMapping("/{userId}/{themeId}/groups/{groupId}")
    public ResponseEntity<Void> deleteGroup(@PathVariable Long userId, @PathVariable Long themeId, @PathVariable Long groupId) {
        service.deleteGroup(userId, themeId, groupId);
        return ResponseEntity.noContent().build();
    }
}
