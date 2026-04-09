package com.bloodbank.complianceservice.controller;

import com.bloodbank.common.dto.ApiResponse;
import com.bloodbank.complianceservice.dto.LicenseCreateRequest;
import com.bloodbank.complianceservice.dto.LicenseResponse;
import com.bloodbank.complianceservice.enums.LicenseStatusEnum;
import com.bloodbank.complianceservice.enums.LicenseTypeEnum;
import com.bloodbank.complianceservice.service.LicenseService;

import jakarta.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;
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

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/compliance/licenses")
public class LicenseController {

    private final LicenseService licenseService;

    public LicenseController(LicenseService licenseService) {
        this.licenseService = licenseService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('AUDITOR','BRANCH_ADMIN')")
    public ResponseEntity<ApiResponse<LicenseResponse>> create(
            @Valid @RequestBody LicenseCreateRequest request) {
        LicenseResponse response = licenseService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "License created successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('AUDITOR','BRANCH_ADMIN')")
    public ResponseEntity<ApiResponse<LicenseResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(licenseService.getById(id)));
    }

    @GetMapping("/number/{licenseNumber}")
    @PreAuthorize("hasAnyRole('AUDITOR','BRANCH_ADMIN')")
    public ResponseEntity<ApiResponse<LicenseResponse>> getByNumber(@PathVariable String licenseNumber) {
        return ResponseEntity.ok(ApiResponse.success(licenseService.getByLicenseNumber(licenseNumber)));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('AUDITOR','BRANCH_ADMIN')")
    public ResponseEntity<ApiResponse<List<LicenseResponse>>> getByStatus(@PathVariable LicenseStatusEnum status) {
        return ResponseEntity.ok(ApiResponse.success(licenseService.getByStatus(status)));
    }

    @GetMapping("/type/{type}")
    @PreAuthorize("hasAnyRole('AUDITOR','BRANCH_ADMIN')")
    public ResponseEntity<ApiResponse<List<LicenseResponse>>> getByType(@PathVariable LicenseTypeEnum type) {
        return ResponseEntity.ok(ApiResponse.success(licenseService.getByType(type)));
    }

    @GetMapping("/expiring")
    @PreAuthorize("hasAnyRole('AUDITOR','BRANCH_ADMIN')")
    public ResponseEntity<ApiResponse<List<LicenseResponse>>> getExpiringSoon(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate beforeDate) {
        return ResponseEntity.ok(ApiResponse.success(licenseService.getExpiringSoon(beforeDate)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('AUDITOR','BRANCH_ADMIN')")
    public ResponseEntity<ApiResponse<LicenseResponse>> updateStatus(
            @PathVariable UUID id, @RequestParam LicenseStatusEnum status) {
        return ResponseEntity.ok(ApiResponse.success(
                licenseService.updateStatus(id, status), "License status updated successfully"));
    }

    @PatchMapping("/{id}/renew")
    @PreAuthorize("hasAnyRole('AUDITOR','BRANCH_ADMIN')")
    public ResponseEntity<ApiResponse<LicenseResponse>> renew(
            @PathVariable UUID id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate newExpiryDate) {
        return ResponseEntity.ok(ApiResponse.success(
                licenseService.renew(id, newExpiryDate), "License renewed successfully"));
    }
}
