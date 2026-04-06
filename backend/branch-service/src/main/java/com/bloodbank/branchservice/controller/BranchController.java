package com.bloodbank.branchservice.controller;

import com.bloodbank.branchservice.dto.*;
import com.bloodbank.branchservice.service.BranchService;
import com.bloodbank.common.dto.ApiResponse;
import com.bloodbank.common.dto.PagedResponse;
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
@RequestMapping("/api/v1/branches")
@Tag(name = "Branch Management", description = "Branch lifecycle and configuration operations")
public class BranchController {

    private final BranchService branchService;

    public BranchController(BranchService branchService) {
        this.branchService = branchService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REGIONAL_ADMIN')")
    @Operation(summary = "Create a new branch")
    public ResponseEntity<ApiResponse<BranchResponse>> createBranch(
            @Valid @RequestBody BranchCreateRequest request) {
        BranchResponse response = branchService.createBranch(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Branch created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REGIONAL_ADMIN','BRANCH_ADMIN')")
    @Operation(summary = "Update a branch")
    public ResponseEntity<ApiResponse<BranchResponse>> updateBranch(
            @PathVariable UUID id,
            @Valid @RequestBody BranchUpdateRequest request) {
        BranchResponse response = branchService.updateBranch(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Branch updated successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REGIONAL_ADMIN','BRANCH_ADMIN','BRANCH_MANAGER')")
    @Operation(summary = "Get branch by ID")
    public ResponseEntity<ApiResponse<BranchResponse>> getBranchById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(branchService.getBranchById(id)));
    }

    @GetMapping("/code/{code}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REGIONAL_ADMIN','BRANCH_ADMIN','BRANCH_MANAGER')")
    @Operation(summary = "Get branch by code")
    public ResponseEntity<ApiResponse<BranchResponse>> getBranchByCode(@PathVariable String code) {
        return ResponseEntity.ok(ApiResponse.success(branchService.getBranchByCode(code)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REGIONAL_ADMIN','BRANCH_ADMIN','BRANCH_MANAGER')")
    @Operation(summary = "Get all branches (paginated)")
    public ResponseEntity<ApiResponse<PagedResponse<BranchResponse>>> getAllBranches(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(branchService.getAllBranches(pageable)));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REGIONAL_ADMIN','BRANCH_ADMIN','BRANCH_MANAGER')")
    @Operation(summary = "Search branches by name")
    public ResponseEntity<ApiResponse<PagedResponse<BranchResponse>>> searchBranches(
            @RequestParam String name,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(branchService.searchBranches(name, pageable)));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REGIONAL_ADMIN')")
    @Operation(summary = "Get branches by status")
    public ResponseEntity<ApiResponse<List<BranchResponse>>> getBranchesByStatus(
            @PathVariable String status) {
        return ResponseEntity.ok(ApiResponse.success(branchService.getBranchesByStatus(status)));
    }

    @GetMapping("/type/{branchType}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REGIONAL_ADMIN')")
    @Operation(summary = "Get branches by type")
    public ResponseEntity<ApiResponse<List<BranchResponse>>> getBranchesByType(
            @PathVariable String branchType) {
        return ResponseEntity.ok(ApiResponse.success(branchService.getBranchesByType(branchType)));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REGIONAL_ADMIN')")
    @Operation(summary = "Activate a branch")
    public ResponseEntity<ApiResponse<BranchResponse>> activateBranch(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(branchService.activateBranch(id), "Branch activated successfully"));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REGIONAL_ADMIN')")
    @Operation(summary = "Deactivate a branch")
    public ResponseEntity<ApiResponse<BranchResponse>> deactivateBranch(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(branchService.deactivateBranch(id), "Branch deactivated successfully"));
    }

    @PostMapping("/{branchId}/operating-hours")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REGIONAL_ADMIN','BRANCH_ADMIN')")
    @Operation(summary = "Add operating hours for a branch")
    public ResponseEntity<ApiResponse<BranchOperatingHoursResponse>> addOperatingHours(
            @PathVariable UUID branchId,
            @Valid @RequestBody BranchOperatingHoursRequest request) {
        BranchOperatingHoursResponse response = branchService.addOperatingHours(branchId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Operating hours added successfully"));
    }

    @GetMapping("/{branchId}/operating-hours")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REGIONAL_ADMIN','BRANCH_ADMIN','BRANCH_MANAGER')")
    @Operation(summary = "Get operating hours for a branch")
    public ResponseEntity<ApiResponse<List<BranchOperatingHoursResponse>>> getOperatingHours(
            @PathVariable UUID branchId) {
        return ResponseEntity.ok(ApiResponse.success(branchService.getOperatingHours(branchId)));
    }

    @PostMapping("/{branchId}/equipment")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REGIONAL_ADMIN','BRANCH_ADMIN')")
    @Operation(summary = "Add equipment to a branch")
    public ResponseEntity<ApiResponse<BranchEquipmentResponse>> addEquipment(
            @PathVariable UUID branchId,
            @Valid @RequestBody BranchEquipmentRequest request) {
        BranchEquipmentResponse response = branchService.addEquipment(branchId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Equipment added successfully"));
    }

    @GetMapping("/{branchId}/equipment")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REGIONAL_ADMIN','BRANCH_ADMIN','BRANCH_MANAGER')")
    @Operation(summary = "Get equipment for a branch")
    public ResponseEntity<ApiResponse<List<BranchEquipmentResponse>>> getEquipment(
            @PathVariable UUID branchId) {
        return ResponseEntity.ok(ApiResponse.success(branchService.getEquipment(branchId)));
    }

    @PostMapping("/{branchId}/regions")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REGIONAL_ADMIN','BRANCH_ADMIN')")
    @Operation(summary = "Assign a region to a branch")
    public ResponseEntity<ApiResponse<BranchRegionResponse>> addBranchRegion(
            @PathVariable UUID branchId,
            @Valid @RequestBody BranchRegionRequest request) {
        BranchRegionResponse response = branchService.addBranchRegion(branchId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Region assigned to branch successfully"));
    }

    @GetMapping("/{branchId}/regions")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REGIONAL_ADMIN','BRANCH_ADMIN','BRANCH_MANAGER')")
    @Operation(summary = "Get regions for a branch")
    public ResponseEntity<ApiResponse<List<BranchRegionResponse>>> getBranchRegions(
            @PathVariable UUID branchId) {
        return ResponseEntity.ok(ApiResponse.success(branchService.getBranchRegions(branchId)));
    }

    @DeleteMapping("/{branchId}/regions/{regionId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REGIONAL_ADMIN','BRANCH_ADMIN')")
    @Operation(summary = "Remove a region from a branch")
    public ResponseEntity<ApiResponse<Void>> removeBranchRegion(
            @PathVariable UUID branchId,
            @PathVariable UUID regionId) {
        branchService.removeBranchRegion(branchId, regionId);
        return ResponseEntity.ok(ApiResponse.success(null, "Region removed from branch successfully"));
    }
}
