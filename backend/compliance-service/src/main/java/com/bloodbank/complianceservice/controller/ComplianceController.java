package com.bloodbank.complianceservice.controller;

import com.bloodbank.common.dto.ApiResponse;
import com.bloodbank.complianceservice.dto.RegulatoryFrameworkCreateRequest;
import com.bloodbank.complianceservice.dto.RegulatoryFrameworkResponse;
import com.bloodbank.complianceservice.service.ComplianceService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/compliance/frameworks")
public class ComplianceController {

    private final ComplianceService complianceService;

    public ComplianceController(ComplianceService complianceService) {
        this.complianceService = complianceService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('AUDITOR','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<RegulatoryFrameworkResponse>> create(
            @Valid @RequestBody RegulatoryFrameworkCreateRequest request) {
        RegulatoryFrameworkResponse response = complianceService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Regulatory framework created successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('AUDITOR','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<RegulatoryFrameworkResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(complianceService.getById(id)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('AUDITOR','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<RegulatoryFrameworkResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(complianceService.getAll()));
    }

    @GetMapping("/code/{code}")
    @PreAuthorize("hasAnyRole('AUDITOR','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<RegulatoryFrameworkResponse>> getByCode(@PathVariable String code) {
        return ResponseEntity.ok(ApiResponse.success(complianceService.getByFrameworkCode(code)));
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('AUDITOR','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<RegulatoryFrameworkResponse>>> getActiveFrameworks() {
        return ResponseEntity.ok(ApiResponse.success(complianceService.getActiveFrameworks()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('AUDITOR','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<RegulatoryFrameworkResponse>> update(
            @PathVariable UUID id, @Valid @RequestBody RegulatoryFrameworkCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                complianceService.update(id, request), "Regulatory framework updated successfully"));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('AUDITOR','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<RegulatoryFrameworkResponse>> deactivate(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(
                complianceService.deactivate(id), "Regulatory framework deactivated successfully"));
    }
}
