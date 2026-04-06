package com.bloodbank.donorservice.controller;

import com.bloodbank.common.dto.ApiResponse;
import com.bloodbank.common.dto.PagedResponse;
import com.bloodbank.donorservice.dto.BloodCampCreateRequest;
import com.bloodbank.donorservice.dto.BloodCampResponse;
import com.bloodbank.donorservice.dto.BloodCampUpdateRequest;
import com.bloodbank.donorservice.dto.CampDonorCreateRequest;
import com.bloodbank.donorservice.dto.CampDonorResponse;
import com.bloodbank.donorservice.dto.CampResourceCreateRequest;
import com.bloodbank.donorservice.dto.CampResourceResponse;
import com.bloodbank.donorservice.enums.CampStatusEnum;
import com.bloodbank.donorservice.service.BloodCampService;
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
@RequestMapping("/api/v1/camps")
@Tag(name = "Blood Camp Management", description = "Blood camp planning and management")
public class BloodCampController {

    private final BloodCampService bloodCampService;

    public BloodCampController(BloodCampService bloodCampService) {
        this.bloodCampService = bloodCampService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CAMP_COORDINATOR','BRANCH_MANAGER')")
    @Operation(summary = "Create a new blood camp")
    public ResponseEntity<ApiResponse<BloodCampResponse>> createCamp(
            @Valid @RequestBody BloodCampCreateRequest request) {
        BloodCampResponse response = bloodCampService.createCamp(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Blood camp created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('CAMP_COORDINATOR','BRANCH_MANAGER')")
    @Operation(summary = "Update a blood camp")
    public ResponseEntity<ApiResponse<BloodCampResponse>> updateCamp(
            @PathVariable UUID id,
            @Valid @RequestBody BloodCampUpdateRequest request) {
        BloodCampResponse response = bloodCampService.updateCamp(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Blood camp updated successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CAMP_COORDINATOR','BRANCH_MANAGER','BRANCH_ADMIN')")
    @Operation(summary = "Get blood camp by ID")
    public ResponseEntity<ApiResponse<BloodCampResponse>> getCampById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(bloodCampService.getCampById(id)));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('CAMP_COORDINATOR','BRANCH_MANAGER','BRANCH_ADMIN')")
    @Operation(summary = "Get blood camps by status")
    public ResponseEntity<ApiResponse<PagedResponse<BloodCampResponse>>> getCampsByStatus(
            @PathVariable CampStatusEnum status,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(bloodCampService.getCampsByStatus(status, pageable)));
    }

    @PatchMapping("/{id}/start")
    @PreAuthorize("hasAnyRole('CAMP_COORDINATOR','BRANCH_MANAGER')")
    @Operation(summary = "Start a blood camp")
    public ResponseEntity<ApiResponse<BloodCampResponse>> startCamp(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(bloodCampService.startCamp(id), "Blood camp started successfully"));
    }

    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('CAMP_COORDINATOR','BRANCH_MANAGER')")
    @Operation(summary = "Complete a blood camp")
    public ResponseEntity<ApiResponse<BloodCampResponse>> completeCamp(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(bloodCampService.completeCamp(id), "Blood camp completed successfully"));
    }

    @PostMapping("/{campId}/resources")
    @PreAuthorize("hasAnyRole('CAMP_COORDINATOR','BRANCH_MANAGER')")
    @Operation(summary = "Add a resource to a blood camp")
    public ResponseEntity<ApiResponse<CampResourceResponse>> addResource(
            @PathVariable UUID campId,
            @Valid @RequestBody CampResourceCreateRequest request) {
        CampResourceResponse response = bloodCampService.addResource(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Resource added successfully"));
    }

    @GetMapping("/{campId}/resources")
    @PreAuthorize("hasAnyRole('CAMP_COORDINATOR','BRANCH_MANAGER','BRANCH_ADMIN')")
    @Operation(summary = "Get resources for a blood camp")
    public ResponseEntity<ApiResponse<List<CampResourceResponse>>> getResources(
            @PathVariable UUID campId) {
        return ResponseEntity.ok(ApiResponse.success(bloodCampService.getResources(campId)));
    }

    @PostMapping("/{campId}/donors")
    @PreAuthorize("hasAnyRole('CAMP_COORDINATOR','RECEPTIONIST','PHLEBOTOMIST')")
    @Operation(summary = "Register a donor for a blood camp")
    public ResponseEntity<ApiResponse<CampDonorResponse>> registerDonor(
            @PathVariable UUID campId,
            @Valid @RequestBody CampDonorCreateRequest request) {
        CampDonorResponse response = bloodCampService.registerDonor(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Donor registered for camp successfully"));
    }

    @GetMapping("/{campId}/donors")
    @PreAuthorize("hasAnyRole('CAMP_COORDINATOR','BRANCH_MANAGER')")
    @Operation(summary = "Get donors for a blood camp")
    public ResponseEntity<ApiResponse<List<CampDonorResponse>>> getCampDonors(
            @PathVariable UUID campId) {
        return ResponseEntity.ok(ApiResponse.success(bloodCampService.getCampDonors(campId)));
    }

    @PostMapping("/{campId}/collections/{collectionId}")
    @PreAuthorize("hasAnyRole('CAMP_COORDINATOR','PHLEBOTOMIST')")
    @Operation(summary = "Link a collection to a blood camp")
    public ResponseEntity<ApiResponse<Void>> linkCollection(
            @PathVariable UUID campId,
            @PathVariable UUID collectionId) {
        bloodCampService.linkCollection(campId, collectionId);
        return ResponseEntity.ok(ApiResponse.success(null, "Collection linked to camp successfully"));
    }
}
