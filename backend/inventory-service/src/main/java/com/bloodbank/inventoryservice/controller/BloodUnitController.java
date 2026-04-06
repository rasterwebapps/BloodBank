package com.bloodbank.inventoryservice.controller;

import com.bloodbank.common.dto.ApiResponse;
import com.bloodbank.common.model.enums.BloodUnitStatusEnum;
import com.bloodbank.inventoryservice.dto.BloodUnitCreateRequest;
import com.bloodbank.inventoryservice.dto.BloodUnitResponse;
import com.bloodbank.inventoryservice.dto.BloodUnitStatusUpdateRequest;
import com.bloodbank.inventoryservice.service.BloodUnitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/blood-units")
@Tag(name = "Blood Unit Management", description = "Blood unit lifecycle operations")
public class BloodUnitController {

    private final BloodUnitService bloodUnitService;

    public BloodUnitController(BloodUnitService bloodUnitService) {
        this.bloodUnitService = bloodUnitService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('INVENTORY_MANAGER','LAB_TECHNICIAN')")
    @Operation(summary = "Create a new blood unit")
    public ResponseEntity<ApiResponse<BloodUnitResponse>> create(
            @Valid @RequestBody BloodUnitCreateRequest request) {
        BloodUnitResponse response = bloodUnitService.createBloodUnit(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Blood unit created successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('INVENTORY_MANAGER','LAB_TECHNICIAN')")
    @Operation(summary = "Get blood unit by ID")
    public ResponseEntity<ApiResponse<BloodUnitResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(bloodUnitService.getById(id)));
    }

    @GetMapping("/unit-number/{unitNumber}")
    @PreAuthorize("hasAnyRole('INVENTORY_MANAGER','LAB_TECHNICIAN')")
    @Operation(summary = "Get blood unit by unit number")
    public ResponseEntity<ApiResponse<BloodUnitResponse>> getByUnitNumber(
            @PathVariable String unitNumber) {
        return ResponseEntity.ok(ApiResponse.success(bloodUnitService.getByUnitNumber(unitNumber)));
    }

    @GetMapping("/donor/{donorId}")
    @PreAuthorize("hasAnyRole('INVENTORY_MANAGER','LAB_TECHNICIAN')")
    @Operation(summary = "Get blood units by donor")
    public ResponseEntity<ApiResponse<List<BloodUnitResponse>>> getByDonor(
            @PathVariable UUID donorId) {
        return ResponseEntity.ok(ApiResponse.success(bloodUnitService.getByDonor(donorId)));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('INVENTORY_MANAGER','LAB_TECHNICIAN')")
    @Operation(summary = "Get blood units by status")
    public ResponseEntity<ApiResponse<List<BloodUnitResponse>>> getByStatus(
            @PathVariable BloodUnitStatusEnum status) {
        return ResponseEntity.ok(ApiResponse.success(bloodUnitService.getByStatus(status)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('INVENTORY_MANAGER','LAB_TECHNICIAN')")
    @Operation(summary = "Update blood unit status")
    public ResponseEntity<ApiResponse<BloodUnitResponse>> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody BloodUnitStatusUpdateRequest request) {
        BloodUnitResponse response = bloodUnitService.updateStatus(id, request.status());
        return ResponseEntity.ok(ApiResponse.success(response, "Blood unit status updated"));
    }
}
