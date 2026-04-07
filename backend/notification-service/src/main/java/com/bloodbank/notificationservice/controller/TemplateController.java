package com.bloodbank.notificationservice.controller;

import com.bloodbank.common.dto.ApiResponse;
import com.bloodbank.notificationservice.dto.NotificationTemplateCreateRequest;
import com.bloodbank.notificationservice.dto.NotificationTemplateResponse;
import com.bloodbank.notificationservice.service.TemplateService;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notification-templates")
public class TemplateController {

    private final TemplateService templateService;

    public TemplateController(TemplateService templateService) {
        this.templateService = templateService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('BRANCH_ADMIN','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<NotificationTemplateResponse>> create(
            @Valid @RequestBody NotificationTemplateCreateRequest request) {
        NotificationTemplateResponse response = templateService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Notification template created successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('BRANCH_ADMIN','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<NotificationTemplateResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(templateService.getById(id)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('BRANCH_ADMIN','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<List<NotificationTemplateResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(templateService.getAll()));
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('BRANCH_ADMIN','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<List<NotificationTemplateResponse>>> getActiveTemplates() {
        return ResponseEntity.ok(ApiResponse.success(templateService.getActiveTemplates()));
    }

    @GetMapping("/language")
    @PreAuthorize("hasAnyRole('BRANCH_ADMIN','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<List<NotificationTemplateResponse>>> getByLanguage(
            @RequestParam String language) {
        return ResponseEntity.ok(ApiResponse.success(templateService.getByLanguage(language)));
    }

    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('BRANCH_ADMIN','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<NotificationTemplateResponse>> deactivate(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(templateService.deactivate(id),
                "Template deactivated successfully"));
    }
}
