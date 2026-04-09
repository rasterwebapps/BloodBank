package com.bloodbank.complianceservice.controller;

import com.bloodbank.common.dto.ApiResponse;
import com.bloodbank.complianceservice.dto.CorrectiveActionRequest;
import com.bloodbank.complianceservice.dto.DeviationCreateRequest;
import com.bloodbank.complianceservice.dto.DeviationResponse;
import com.bloodbank.complianceservice.enums.DeviationSeverityEnum;
import com.bloodbank.complianceservice.enums.DeviationStatusEnum;
import com.bloodbank.complianceservice.service.DeviationService;

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
@RequestMapping("/api/v1/compliance/deviations")
public class DeviationController {

    private final DeviationService deviationService;

    public DeviationController(DeviationService deviationService) {
        this.deviationService = deviationService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('AUDITOR','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<DeviationResponse>> create(
            @Valid @RequestBody DeviationCreateRequest request) {
        DeviationResponse response = deviationService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Deviation created successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('AUDITOR','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<DeviationResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(deviationService.getById(id)));
    }

    @GetMapping("/number/{number}")
    @PreAuthorize("hasAnyRole('AUDITOR','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<DeviationResponse>> getByNumber(@PathVariable String number) {
        return ResponseEntity.ok(ApiResponse.success(deviationService.getByDeviationNumber(number)));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('AUDITOR','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<List<DeviationResponse>>> getByStatus(@PathVariable DeviationStatusEnum status) {
        return ResponseEntity.ok(ApiResponse.success(deviationService.getByStatus(status)));
    }

    @GetMapping("/severity/{severity}")
    @PreAuthorize("hasAnyRole('AUDITOR','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<List<DeviationResponse>>> getBySeverity(@PathVariable DeviationSeverityEnum severity) {
        return ResponseEntity.ok(ApiResponse.success(deviationService.getBySeverity(severity)));
    }

    @PatchMapping("/{id}/investigate")
    @PreAuthorize("hasAnyRole('AUDITOR','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<DeviationResponse>> investigate(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(
                deviationService.investigate(id), "Deviation under investigation"));
    }

    @PatchMapping("/{id}/corrective-action")
    @PreAuthorize("hasAnyRole('AUDITOR','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<DeviationResponse>> addCorrectiveAction(
            @PathVariable UUID id, @Valid @RequestBody CorrectiveActionRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                deviationService.addCorrectiveAction(id, request), "Corrective action added successfully"));
    }

    @PatchMapping("/{id}/close")
    @PreAuthorize("hasAnyRole('AUDITOR','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<DeviationResponse>> close(
            @PathVariable UUID id, @RequestParam String closedBy) {
        return ResponseEntity.ok(ApiResponse.success(
                deviationService.close(id, closedBy), "Deviation closed successfully"));
    }

    @PatchMapping("/{id}/reopen")
    @PreAuthorize("hasAnyRole('AUDITOR','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<DeviationResponse>> reopen(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(
                deviationService.reopen(id), "Deviation reopened successfully"));
    }
}
