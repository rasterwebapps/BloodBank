package com.bloodbank.inventoryservice.controller;

import com.bloodbank.common.dto.ApiResponse;
import com.bloodbank.inventoryservice.dto.*;
import com.bloodbank.inventoryservice.service.BloodComponentService;
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
@RequestMapping("/api/v1/components")
@Tag(name = "Component Management", description = "Blood component processing operations")
public class ComponentController {

    private final BloodComponentService bloodComponentService;

    public ComponentController(BloodComponentService bloodComponentService) {
        this.bloodComponentService = bloodComponentService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('INVENTORY_MANAGER')")
    @Operation(summary = "Create a new blood component")
    public ResponseEntity<ApiResponse<BloodComponentResponse>> create(
            @Valid @RequestBody BloodComponentCreateRequest request) {
        BloodComponentResponse response = bloodComponentService.createComponent(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Blood component created successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('INVENTORY_MANAGER')")
    @Operation(summary = "Get blood component by ID")
    public ResponseEntity<ApiResponse<BloodComponentResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(bloodComponentService.getById(id)));
    }

    @GetMapping("/blood-unit/{bloodUnitId}")
    @PreAuthorize("hasAnyRole('INVENTORY_MANAGER')")
    @Operation(summary = "Get components by blood unit")
    public ResponseEntity<ApiResponse<List<BloodComponentResponse>>> getByBloodUnit(
            @PathVariable UUID bloodUnitId) {
        return ResponseEntity.ok(ApiResponse.success(
                bloodComponentService.getByBloodUnit(bloodUnitId)));
    }

    @PostMapping("/{id}/process")
    @PreAuthorize("hasAnyRole('INVENTORY_MANAGER')")
    @Operation(summary = "Process a component")
    public ResponseEntity<ApiResponse<ComponentProcessingResponse>> processComponent(
            @PathVariable UUID id,
            @Valid @RequestBody ComponentProcessingCreateRequest request) {
        ComponentProcessingResponse response = bloodComponentService.processComponent(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Component processed successfully"));
    }

    @PostMapping("/{id}/label")
    @PreAuthorize("hasAnyRole('INVENTORY_MANAGER')")
    @Operation(summary = "Create a label for a component")
    public ResponseEntity<ApiResponse<ComponentLabelResponse>> createLabel(
            @PathVariable UUID id,
            @Valid @RequestBody ComponentLabelCreateRequest request) {
        ComponentLabelResponse response = bloodComponentService.createLabel(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Component label created successfully"));
    }

    @PostMapping("/pooled")
    @PreAuthorize("hasAnyRole('INVENTORY_MANAGER')")
    @Operation(summary = "Create a pooled component")
    public ResponseEntity<ApiResponse<PooledComponentResponse>> createPooledComponent(
            @Valid @RequestBody PooledComponentCreateRequest request) {
        PooledComponentResponse response = bloodComponentService.createPooledComponent(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Pooled component created successfully"));
    }
}
