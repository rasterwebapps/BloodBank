package com.bloodbank.transfusionservice.controller;

import com.bloodbank.common.dto.ApiResponse;
import com.bloodbank.transfusionservice.dto.HemovigilanceReportCreateRequest;
import com.bloodbank.transfusionservice.dto.HemovigilanceReportResponse;
import com.bloodbank.transfusionservice.dto.LookBackInvestigationCreateRequest;
import com.bloodbank.transfusionservice.dto.LookBackInvestigationResponse;
import com.bloodbank.transfusionservice.service.HemovigilanceService;

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
@RequestMapping("/api/v1/hemovigilance")
@Tag(name = "Hemovigilance Management", description = "Hemovigilance reports and lookback investigations")
public class HemovigilanceController {

    private final HemovigilanceService hemovigilanceService;

    public HemovigilanceController(HemovigilanceService hemovigilanceService) {
        this.hemovigilanceService = hemovigilanceService;
    }

    @PostMapping("/reports")
    @PreAuthorize("hasAnyRole('DOCTOR','BRANCH_MANAGER')")
    @Operation(summary = "Create a hemovigilance report")
    public ResponseEntity<ApiResponse<HemovigilanceReportResponse>> createReport(
            @Valid @RequestBody HemovigilanceReportCreateRequest request) {
        HemovigilanceReportResponse response = hemovigilanceService.createReport(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Hemovigilance report created successfully"));
    }

    @PutMapping("/reports/{id}/status")
    @PreAuthorize("hasAnyRole('DOCTOR','BRANCH_MANAGER')")
    @Operation(summary = "Update hemovigilance report status")
    public ResponseEntity<ApiResponse<HemovigilanceReportResponse>> updateReportStatus(
            @PathVariable UUID id,
            @RequestParam String status) {
        HemovigilanceReportResponse response = hemovigilanceService.updateReportStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success(response, "Report status updated successfully"));
    }

    @GetMapping("/reports/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR','BRANCH_MANAGER')")
    @Operation(summary = "Get hemovigilance report by ID")
    public ResponseEntity<ApiResponse<HemovigilanceReportResponse>> getReportById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(hemovigilanceService.getReportById(id)));
    }

    @PostMapping("/lookback")
    @PreAuthorize("hasAnyRole('DOCTOR','BRANCH_MANAGER')")
    @Operation(summary = "Create a lookback investigation")
    public ResponseEntity<ApiResponse<LookBackInvestigationResponse>> createLookBackInvestigation(
            @Valid @RequestBody LookBackInvestigationCreateRequest request) {
        LookBackInvestigationResponse response = hemovigilanceService.createLookBackInvestigation(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Lookback investigation created successfully"));
    }

    @PutMapping("/lookback/{id}/status")
    @PreAuthorize("hasAnyRole('DOCTOR','BRANCH_MANAGER')")
    @Operation(summary = "Update lookback investigation status")
    public ResponseEntity<ApiResponse<LookBackInvestigationResponse>> updateLookBackStatus(
            @PathVariable UUID id,
            @RequestParam String status,
            @RequestParam(required = false) String findings) {
        LookBackInvestigationResponse response = hemovigilanceService.updateLookBackStatus(id, status, findings);
        return ResponseEntity.ok(ApiResponse.success(response, "Investigation status updated successfully"));
    }

    @GetMapping("/lookback/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR','BRANCH_MANAGER')")
    @Operation(summary = "Get lookback investigation by ID")
    public ResponseEntity<ApiResponse<LookBackInvestigationResponse>> getInvestigationById(
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(hemovigilanceService.getInvestigationById(id)));
    }
}
