package com.bloodbank.hospitalservice.controller;

import com.bloodbank.common.dto.ApiResponse;
import com.bloodbank.common.dto.PagedResponse;
import com.bloodbank.hospitalservice.dto.HospitalCreateRequest;
import com.bloodbank.hospitalservice.dto.HospitalResponse;
import com.bloodbank.hospitalservice.enums.HospitalStatusEnum;
import com.bloodbank.hospitalservice.service.HospitalService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
@RequestMapping("/api/v1/hospitals")
@Tag(name = "Hospital Management", description = "Hospital registration and management operations")
public class HospitalController {

    private final HospitalService hospitalService;

    public HospitalController(HospitalService hospitalService) {
        this.hospitalService = hospitalService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REGIONAL_ADMIN','BRANCH_ADMIN','BRANCH_MANAGER')")
    @Operation(summary = "Register a new hospital")
    public ResponseEntity<ApiResponse<HospitalResponse>> createHospital(
            @Valid @RequestBody HospitalCreateRequest request) {
        HospitalResponse response = hospitalService.createHospital(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Hospital registered successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REGIONAL_ADMIN','BRANCH_ADMIN','BRANCH_MANAGER')")
    @Operation(summary = "Update a hospital")
    public ResponseEntity<ApiResponse<HospitalResponse>> updateHospital(
            @PathVariable UUID id,
            @Valid @RequestBody HospitalCreateRequest request) {
        HospitalResponse response = hospitalService.updateHospital(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Hospital updated successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REGIONAL_ADMIN','AUDITOR','BRANCH_ADMIN','BRANCH_MANAGER','DOCTOR','INVENTORY_MANAGER','BILLING_CLERK','HOSPITAL_USER')")
    @Operation(summary = "Get hospital by ID")
    public ResponseEntity<ApiResponse<HospitalResponse>> getHospitalById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(hospitalService.getHospitalById(id)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REGIONAL_ADMIN','AUDITOR','BRANCH_ADMIN','BRANCH_MANAGER','DOCTOR','INVENTORY_MANAGER','BILLING_CLERK','HOSPITAL_USER')")
    @Operation(summary = "List all hospitals")
    public ResponseEntity<ApiResponse<PagedResponse<HospitalResponse>>> getAllHospitals(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(hospitalService.getAllHospitals(pageable)));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REGIONAL_ADMIN','AUDITOR','BRANCH_ADMIN','BRANCH_MANAGER','DOCTOR','INVENTORY_MANAGER','BILLING_CLERK','HOSPITAL_USER')")
    @Operation(summary = "Search hospitals by name")
    public ResponseEntity<ApiResponse<PagedResponse<HospitalResponse>>> searchHospitals(
            @RequestParam String name,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(hospitalService.searchHospitals(name, pageable)));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REGIONAL_ADMIN','AUDITOR','BRANCH_ADMIN','BRANCH_MANAGER')")
    @Operation(summary = "Get hospitals by status")
    public ResponseEntity<ApiResponse<PagedResponse<HospitalResponse>>> getHospitalsByStatus(
            @PathVariable HospitalStatusEnum status,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(hospitalService.getHospitalsByStatus(status, pageable)));
    }

    @GetMapping("/code/{hospitalCode}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REGIONAL_ADMIN','AUDITOR','BRANCH_ADMIN','BRANCH_MANAGER','HOSPITAL_USER')")
    @Operation(summary = "Get hospital by code")
    public ResponseEntity<ApiResponse<HospitalResponse>> getHospitalByCode(
            @PathVariable String hospitalCode) {
        return ResponseEntity.ok(ApiResponse.success(hospitalService.getHospitalByCode(hospitalCode)));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REGIONAL_ADMIN','BRANCH_ADMIN','BRANCH_MANAGER')")
    @Operation(summary = "Update hospital status")
    public ResponseEntity<ApiResponse<HospitalResponse>> updateHospitalStatus(
            @PathVariable UUID id,
            @RequestParam HospitalStatusEnum status) {
        HospitalResponse response = hospitalService.updateHospitalStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success(response, "Hospital status updated successfully"));
    }
}
