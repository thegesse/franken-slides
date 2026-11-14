package com.goose.backend.controller;

import com.goose.backend.dto.SlideGroupResponse;
import com.goose.backend.dto.request.slideGroup.ChangeSlideGroupName;
import com.goose.backend.dto.request.slideGroup.ChangeSlideGroupThemeRequest;
import com.goose.backend.dto.request.slideGroup.CreateSlideGroupRequest;
import com.goose.backend.security.AuthenticatedUser;
import com.goose.backend.service.SlideGroupService;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/users/{userId}/themes/{themeId}/groups")
public class SlideGroupController {
    private final SlideGroupService slideGroupService;
    private final AuthenticatedUser authenticatedUser;

    public SlideGroupController(SlideGroupService slideGroupService, AuthenticatedUser authenticatedUser) {
        this.slideGroupService = slideGroupService;
        this.authenticatedUser = authenticatedUser;
    }

    @GetMapping
    public ResponseEntity<Page<SlideGroupResponse>> getGroupsFromTheme(@PathVariable("userId") Long userId, @PathVariable("themeId") Long themeId, Pageable pageable) {
        Long ownerId = authenticatedUser.requirePathUserId(userId);
        Page<SlideGroupResponse> response = slideGroupService.getSlideGroupForUser(ownerId, themeId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<SlideGroupResponse> getGroupById(@PathVariable("userId") Long userId, @PathVariable("themeId") Long themeId, @PathVariable("groupId") Long groupId) {
        Long ownerId = authenticatedUser.requirePathUserId(userId);
        SlideGroupResponse response = slideGroupService.getSlideGroupForUserById(ownerId, themeId, groupId);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<SlideGroupResponse> createGroup(@PathVariable("userId") Long userId, @PathVariable("themeId") Long themeId, @Valid @RequestBody CreateSlideGroupRequest request) {
        Long ownerId = authenticatedUser.requirePathUserId(userId);
        SlideGroupResponse response = slideGroupService.createSlideGroup(ownerId, themeId, request.name());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{groupId}")
                .buildAndExpand(response.id())
                .toUri();
        // to build new url for new group so post can include in location header
        return ResponseEntity.created(location).body(response);
    }

    @PatchMapping("/{groupId}")
    public ResponseEntity<SlideGroupResponse> changeGroupName(@PathVariable("userId") Long userId, @PathVariable("themeId") Long themeId, @PathVariable("groupId") Long groupId, @Valid @RequestBody ChangeSlideGroupName request) {
        Long ownerId = authenticatedUser.requirePathUserId(userId);
        SlideGroupResponse response = slideGroupService.changeSlideGroupName(ownerId, themeId, groupId, request.name());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{groupId}/theme")
    public ResponseEntity<SlideGroupResponse> changeGroupTheme(@PathVariable("userId") Long userId, @PathVariable("themeId") Long currentThemeId, @PathVariable("groupId") Long groupId, @Valid @RequestBody ChangeSlideGroupThemeRequest request) {
        Long ownerId = authenticatedUser.requirePathUserId(userId);
        SlideGroupResponse response = slideGroupService.changeSlideGroupTheme(ownerId, currentThemeId, groupId, request.targetThemeId());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<Void> deleteGroup(@PathVariable("userId") Long userId, @PathVariable("themeId") Long themeId, @PathVariable("groupId") Long groupId) {
        Long ownerId = authenticatedUser.requirePathUserId(userId);
        slideGroupService.deleteGroup(ownerId, themeId, groupId);
        return ResponseEntity.noContent().build();
    }
}
