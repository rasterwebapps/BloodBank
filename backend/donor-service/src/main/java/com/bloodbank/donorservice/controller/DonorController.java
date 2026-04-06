package com.bloodbank.donorservice.controller;

import com.bloodbank.common.dto.ApiResponse;
import com.bloodbank.common.dto.PagedResponse;
import com.bloodbank.common.model.enums.DonorStatusEnum;
import com.bloodbank.donorservice.dto.DonorConsentCreateRequest;
import com.bloodbank.donorservice.dto.DonorConsentResponse;
import com.bloodbank.donorservice.dto.DonorCreateRequest;
import com.bloodbank.donorservice.dto.DonorDeferralCreateRequest;
import com.bloodbank.donorservice.dto.DonorDeferralResponse;
import com.bloodbank.donorservice.dto.DonorHealthRecordCreateRequest;
import com.bloodbank.donorservice.dto.DonorHealthRecordResponse;
import com.bloodbank.donorservice.dto.DonorResponse;
import com.bloodbank.donorservice.dto.DonorUpdateRequest;
import com.bloodbank.donorservice.service.DonorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/donors")
@Tag(name = "Donor Management", description = "Donor registration and management operations")
public class DonorController {

    private final DonorService donorService;

    public DonorController(DonorService donorService) {
        this.donorService = donorService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('RECEPTIONIST','PHLEBOTOMIST','BRANCH_MANAGER','BRANCH_ADMIN')")
    @Operation(summary = "Register a new donor")
    public ResponseEntity<ApiResponse<DonorResponse>> registerDonor(
            @Valid @RequestBody DonorCreateRequest request) {
        DonorResponse response = donorService.registerDonor(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Donor registered successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('RECEPTIONIST','BRANCH_MANAGER','BRANCH_ADMIN')")
    @Operation(summary = "Update a donor")
    public ResponseEntity<ApiResponse<DonorResponse>> updateDonor(
            @PathVariable UUID id,
            @Valid @RequestBody DonorUpdateRequest request) {
        DonorResponse response = donorService.updateDonor(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Donor updated successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('RECEPTIONIST','PHLEBOTOMIST','BRANCH_MANAGER','BRANCH_ADMIN','DOCTOR')")
    @Operation(summary = "Get donor by ID")
    public ResponseEntity<ApiResponse<DonorResponse>> getDonorById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(donorService.getDonorById(id)));
    }

    @GetMapping("/number/{donorNumber}")
    @PreAuthorize("hasAnyRole('RECEPTIONIST','PHLEBOTOMIST','BRANCH_MANAGER','BRANCH_ADMIN','DOCTOR')")
    @Operation(summary = "Get donor by donor number")
    public ResponseEntity<ApiResponse<DonorResponse>> getDonorByDonorNumber(
            @PathVariable String donorNumber) {
        return ResponseEntity.ok(ApiResponse.success(donorService.getDonorByDonorNumber(donorNumber)));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('RECEPTIONIST','PHLEBOTOMIST','BRANCH_MANAGER','BRANCH_ADMIN','DOCTOR')")
    @Operation(summary = "Search donors by name")
    public ResponseEntity<ApiResponse<PagedResponse<DonorResponse>>> searchDonors(
            @RequestParam String query,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(donorService.searchDonors(query, pageable)));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('BRANCH_MANAGER','BRANCH_ADMIN')")
    @Operation(summary = "Get donors by status")
    public ResponseEntity<ApiResponse<PagedResponse<DonorResponse>>> getDonorsByStatus(
            @PathVariable DonorStatusEnum status,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(donorService.getDonorsByStatus(status, pageable)));
    }

    @GetMapping("/{donorId}/eligibility")
    @PreAuthorize("hasAnyRole('PHLEBOTOMIST','DOCTOR','BRANCH_MANAGER')")
    @Operation(summary = "Check donor eligibility")
    public ResponseEntity<ApiResponse<DonorHealthRecordResponse>> checkEligibility(
            @PathVariable UUID donorId) {
        return ResponseEntity.ok(ApiResponse.success(donorService.checkEligibility(donorId)));
    }

    @PostMapping("/{donorId}/defer")
    @PreAuthorize("hasAnyRole('DOCTOR','PHLEBOTOMIST','BRANCH_MANAGER')")
    @Operation(summary = "Defer a donor")
    public ResponseEntity<ApiResponse<DonorDeferralResponse>> deferDonor(
            @PathVariable UUID donorId,
            @Valid @RequestBody DonorDeferralCreateRequest request) {
        DonorDeferralResponse response = donorService.deferDonor(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Donor deferred successfully"));
    }

    @PostMapping("/{donorId}/health-records")
    @PreAuthorize("hasAnyRole('PHLEBOTOMIST','DOCTOR','NURSE')")
    @Operation(summary = "Create a health record for a donor")
    public ResponseEntity<ApiResponse<DonorHealthRecordResponse>> createHealthRecord(
            @PathVariable UUID donorId,
            @Valid @RequestBody DonorHealthRecordCreateRequest request) {
        DonorHealthRecordResponse response = donorService.createHealthRecord(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Health record created successfully"));
    }

    @GetMapping("/{donorId}/health-records")
    @PreAuthorize("hasAnyRole('PHLEBOTOMIST','DOCTOR','BRANCH_MANAGER')")
    @Operation(summary = "Get health records for a donor")
    public ResponseEntity<ApiResponse<List<DonorHealthRecordResponse>>> getHealthRecords(
            @PathVariable UUID donorId) {
        return ResponseEntity.ok(ApiResponse.success(donorService.getHealthRecords(donorId)));
    }

    @PostMapping("/{donorId}/consents")
    @PreAuthorize("hasAnyRole('RECEPTIONIST','PHLEBOTOMIST')")
    @Operation(summary = "Create a consent record for a donor")
    public ResponseEntity<ApiResponse<DonorConsentResponse>> createConsent(
            @PathVariable UUID donorId,
            @Valid @RequestBody DonorConsentCreateRequest request) {
        DonorConsentResponse response = donorService.createConsent(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Consent recorded successfully"));
    }

    @GetMapping("/{donorId}/consents")
    @PreAuthorize("hasAnyRole('RECEPTIONIST','BRANCH_MANAGER','BRANCH_ADMIN')")
    @Operation(summary = "Get consents for a donor")
    public ResponseEntity<ApiResponse<List<DonorConsentResponse>>> getConsents(
            @PathVariable UUID donorId) {
        return ResponseEntity.ok(ApiResponse.success(donorService.getConsents(donorId)));
    }
}
