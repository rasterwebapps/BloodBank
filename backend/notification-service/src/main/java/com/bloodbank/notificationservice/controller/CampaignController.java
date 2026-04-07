package com.bloodbank.notificationservice.controller;

import com.bloodbank.common.dto.ApiResponse;
import com.bloodbank.notificationservice.dto.CampaignCreateRequest;
import com.bloodbank.notificationservice.dto.CampaignResponse;
import com.bloodbank.notificationservice.enums.CampaignStatusEnum;
import com.bloodbank.notificationservice.service.CampaignService;

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
@RequestMapping("/api/v1/campaigns")
public class CampaignController {

    private final CampaignService campaignService;

    public CampaignController(CampaignService campaignService) {
        this.campaignService = campaignService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('BRANCH_ADMIN','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<CampaignResponse>> create(
            @Valid @RequestBody CampaignCreateRequest request) {
        CampaignResponse response = campaignService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Campaign created successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('BRANCH_ADMIN','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<CampaignResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(campaignService.getById(id)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('BRANCH_ADMIN','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<List<CampaignResponse>>> getByStatus(
            @RequestParam CampaignStatusEnum status) {
        return ResponseEntity.ok(ApiResponse.success(campaignService.getByStatus(status)));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('BRANCH_ADMIN','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<CampaignResponse>> updateStatus(
            @PathVariable UUID id, @RequestParam CampaignStatusEnum status) {
        return ResponseEntity.ok(ApiResponse.success(campaignService.updateStatus(id, status),
                "Campaign status updated successfully"));
    }
}
