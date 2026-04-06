package com.bloodbank.inventoryservice.controller;

import com.bloodbank.common.dto.ApiResponse;
import com.bloodbank.inventoryservice.dto.*;
import com.bloodbank.inventoryservice.service.StockService;
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
@RequestMapping("/api/v1/stock")
@Tag(name = "Stock Management", description = "Stock level monitoring and management operations")
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @GetMapping("/levels")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get stock levels")
    public ResponseEntity<ApiResponse<List<StockLevelResponse>>> getStockLevels(
            @RequestParam(required = false) UUID branchId,
            @RequestParam(required = false) UUID bloodGroupId,
            @RequestParam(required = false) UUID componentTypeId) {
        return ResponseEntity.ok(ApiResponse.success(
                stockService.getStockLevels(branchId, bloodGroupId, componentTypeId)));
    }

    @PostMapping("/dispatch")
    @PreAuthorize("hasAnyRole('INVENTORY_MANAGER')")
    @Operation(summary = "Dispatch a component (FEFO)")
    public ResponseEntity<ApiResponse<BloodComponentResponse>> dispatchComponent(
            @RequestParam UUID componentTypeId,
            @RequestParam(required = false) UUID bloodGroupId) {
        BloodComponentResponse response = stockService.dispatchComponent(componentTypeId, bloodGroupId);
        return ResponseEntity.ok(ApiResponse.success(response, "Component dispatched successfully"));
    }

    @GetMapping("/expiring")
    @PreAuthorize("hasAnyRole('INVENTORY_MANAGER','BRANCH_MANAGER')")
    @Operation(summary = "Get expiring units")
    public ResponseEntity<ApiResponse<List<BloodUnitResponse>>> getExpiringUnits(
            @RequestParam(defaultValue = "7") int days) {
        return ResponseEntity.ok(ApiResponse.success(stockService.getExpiringUnits(days)));
    }

    @PostMapping("/dispose")
    @PreAuthorize("hasAnyRole('INVENTORY_MANAGER')")
    @Operation(summary = "Dispose a unit or component")
    public ResponseEntity<ApiResponse<UnitDisposalResponse>> disposeUnit(
            @Valid @RequestBody UnitDisposalCreateRequest request) {
        UnitDisposalResponse response = stockService.disposeUnit(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Unit disposed successfully"));
    }

    @PostMapping("/reserve")
    @PreAuthorize("hasAnyRole('INVENTORY_MANAGER')")
    @Operation(summary = "Reserve a component")
    public ResponseEntity<ApiResponse<UnitReservationResponse>> reserveComponent(
            @Valid @RequestBody UnitReservationCreateRequest request) {
        UnitReservationResponse response = stockService.reserveComponent(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Component reserved successfully"));
    }
}
