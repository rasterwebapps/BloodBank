package com.bloodbank.notificationservice.controller;

import com.bloodbank.common.dto.ApiResponse;
import com.bloodbank.notificationservice.dto.NotificationCreateRequest;
import com.bloodbank.notificationservice.dto.NotificationResponse;
import com.bloodbank.notificationservice.service.NotificationService;

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
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('BRANCH_ADMIN','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<NotificationResponse>> create(
            @Valid @RequestBody NotificationCreateRequest request) {
        NotificationResponse response = notificationService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Notification created successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('BRANCH_ADMIN','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<NotificationResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(notificationService.getById(id)));
    }

    @GetMapping("/recipient/{recipientId}")
    @PreAuthorize("hasAnyRole('BRANCH_ADMIN','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getByRecipientId(
            @PathVariable UUID recipientId) {
        return ResponseEntity.ok(ApiResponse.success(notificationService.getByRecipientId(recipientId)));
    }

    @PutMapping("/{id}/sent")
    @PreAuthorize("hasAnyRole('BRANCH_ADMIN','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsSent(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(notificationService.markAsSent(id),
                "Notification marked as sent"));
    }

    @PutMapping("/{id}/read")
    @PreAuthorize("hasAnyRole('BRANCH_ADMIN','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(notificationService.markAsRead(id),
                "Notification marked as read"));
    }
}
