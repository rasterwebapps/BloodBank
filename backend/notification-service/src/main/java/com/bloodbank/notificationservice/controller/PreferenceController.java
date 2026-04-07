package com.bloodbank.notificationservice.controller;

import com.bloodbank.common.dto.ApiResponse;
import com.bloodbank.notificationservice.dto.NotificationPreferenceCreateRequest;
import com.bloodbank.notificationservice.dto.NotificationPreferenceResponse;
import com.bloodbank.notificationservice.service.PreferenceService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notification-preferences")
public class PreferenceController {

    private final PreferenceService preferenceService;

    public PreferenceController(PreferenceService preferenceService) {
        this.preferenceService = preferenceService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('BRANCH_ADMIN','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<NotificationPreferenceResponse>> create(
            @Valid @RequestBody NotificationPreferenceCreateRequest request) {
        NotificationPreferenceResponse response = preferenceService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Notification preference created successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('BRANCH_ADMIN','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<NotificationPreferenceResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(preferenceService.getById(id)));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('BRANCH_ADMIN','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<List<NotificationPreferenceResponse>>> getByUserId(
            @PathVariable UUID userId) {
        return ResponseEntity.ok(ApiResponse.success(preferenceService.getByUserId(userId)));
    }

    @PutMapping("/{id}/toggle")
    @PreAuthorize("hasAnyRole('BRANCH_ADMIN','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<NotificationPreferenceResponse>> togglePreference(
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(preferenceService.togglePreference(id),
                "Preference toggled successfully"));
    }
}
