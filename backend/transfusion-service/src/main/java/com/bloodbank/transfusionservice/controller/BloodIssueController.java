package com.bloodbank.transfusionservice.controller;

import com.bloodbank.common.dto.ApiResponse;
import com.bloodbank.transfusionservice.dto.BloodIssueCreateRequest;
import com.bloodbank.transfusionservice.dto.BloodIssueResponse;
import com.bloodbank.transfusionservice.dto.EmergencyIssueCreateRequest;
import com.bloodbank.transfusionservice.dto.EmergencyIssueResponse;
import com.bloodbank.transfusionservice.service.BloodIssueService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/blood-issues")
@Tag(name = "Blood Issue Management", description = "Blood issue and emergency issue operations")
public class BloodIssueController {

    private final BloodIssueService bloodIssueService;

    public BloodIssueController(BloodIssueService bloodIssueService) {
        this.bloodIssueService = bloodIssueService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('DOCTOR','NURSE','BRANCH_MANAGER')")
    @Operation(summary = "Issue blood to a patient")
    public ResponseEntity<ApiResponse<BloodIssueResponse>> issueBlood(
            @Valid @RequestBody BloodIssueCreateRequest request) {
        BloodIssueResponse response = bloodIssueService.issueBlood(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Blood issued successfully"));
    }

    @PostMapping("/emergency")
    @PreAuthorize("hasAnyRole('DOCTOR','NURSE','BRANCH_MANAGER')")
    @Operation(summary = "Issue emergency blood (O-negative protocol)")
    public ResponseEntity<ApiResponse<EmergencyIssueResponse>> issueEmergencyBlood(
            @Valid @RequestBody EmergencyIssueCreateRequest request) {
        EmergencyIssueResponse response = bloodIssueService.issueEmergencyBlood(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Emergency blood issued successfully"));
    }

    @PutMapping("/{id}/return")
    @PreAuthorize("hasAnyRole('DOCTOR','NURSE','BRANCH_MANAGER')")
    @Operation(summary = "Return unused blood")
    public ResponseEntity<ApiResponse<BloodIssueResponse>> returnBlood(
            @PathVariable UUID id,
            @RequestParam String returnReason) {
        BloodIssueResponse response = bloodIssueService.returnBlood(id, returnReason);
        return ResponseEntity.ok(ApiResponse.success(response, "Blood returned successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR','NURSE','BRANCH_MANAGER')")
    @Operation(summary = "Get blood issue by ID")
    public ResponseEntity<ApiResponse<BloodIssueResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(bloodIssueService.getById(id)));
    }

    @GetMapping("/number/{issueNumber}")
    @PreAuthorize("hasAnyRole('DOCTOR','NURSE','BRANCH_MANAGER')")
    @Operation(summary = "Get blood issue by issue number")
    public ResponseEntity<ApiResponse<BloodIssueResponse>> getByIssueNumber(
            @PathVariable String issueNumber) {
        return ResponseEntity.ok(ApiResponse.success(bloodIssueService.getByIssueNumber(issueNumber)));
    }
}
