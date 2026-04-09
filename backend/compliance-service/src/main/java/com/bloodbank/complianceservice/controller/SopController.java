package com.bloodbank.complianceservice.controller;

import com.bloodbank.common.dto.ApiResponse;
import com.bloodbank.complianceservice.dto.SopCreateRequest;
import com.bloodbank.complianceservice.dto.SopResponse;
import com.bloodbank.complianceservice.enums.SopCategoryEnum;
import com.bloodbank.complianceservice.enums.SopStatusEnum;
import com.bloodbank.complianceservice.service.SopService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/compliance/sops")
public class SopController {

    private final SopService sopService;

    public SopController(SopService sopService) {
        this.sopService = sopService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('AUDITOR','BRANCH_ADMIN')")
    public ResponseEntity<ApiResponse<SopResponse>> create(
            @Valid @RequestBody SopCreateRequest request) {
        SopResponse response = sopService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "SOP document created successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('AUDITOR','BRANCH_ADMIN')")
    public ResponseEntity<ApiResponse<SopResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(sopService.getById(id)));
    }

    @GetMapping("/framework/{frameworkId}")
    @PreAuthorize("hasAnyRole('AUDITOR','BRANCH_ADMIN')")
    public ResponseEntity<ApiResponse<List<SopResponse>>> getByFramework(@PathVariable UUID frameworkId) {
        return ResponseEntity.ok(ApiResponse.success(sopService.getByFrameworkId(frameworkId)));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('AUDITOR','BRANCH_ADMIN')")
    public ResponseEntity<ApiResponse<List<SopResponse>>> getByStatus(@PathVariable SopStatusEnum status) {
        return ResponseEntity.ok(ApiResponse.success(sopService.getByStatus(status)));
    }

    @GetMapping("/category/{category}")
    @PreAuthorize("hasAnyRole('AUDITOR','BRANCH_ADMIN')")
    public ResponseEntity<ApiResponse<List<SopResponse>>> getByCategory(@PathVariable SopCategoryEnum category) {
        return ResponseEntity.ok(ApiResponse.success(sopService.getByCategory(category)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('AUDITOR','BRANCH_ADMIN')")
    public ResponseEntity<ApiResponse<SopResponse>> updateStatus(
            @PathVariable UUID id, @RequestParam SopStatusEnum status) {
        return ResponseEntity.ok(ApiResponse.success(
                sopService.updateStatus(id, status), "SOP status updated successfully"));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('AUDITOR','BRANCH_ADMIN')")
    public ResponseEntity<ApiResponse<SopResponse>> approve(
            @PathVariable UUID id, @RequestParam String approvedBy) {
        return ResponseEntity.ok(ApiResponse.success(
                sopService.approve(id, approvedBy), "SOP approved successfully"));
    }

    @PatchMapping("/{id}/retire")
    @PreAuthorize("hasAnyRole('AUDITOR','BRANCH_ADMIN')")
    public ResponseEntity<ApiResponse<SopResponse>> retire(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(
                sopService.retire(id), "SOP retired successfully"));
    }
}
