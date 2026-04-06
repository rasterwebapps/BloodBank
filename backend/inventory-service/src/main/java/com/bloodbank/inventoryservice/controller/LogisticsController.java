package com.bloodbank.inventoryservice.controller;

import com.bloodbank.common.dto.ApiResponse;
import com.bloodbank.inventoryservice.dto.*;
import com.bloodbank.inventoryservice.enums.TransportStatusEnum;
import com.bloodbank.inventoryservice.service.LogisticsService;
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
@RequestMapping("/api/v1/logistics")
@Tag(name = "Logistics Management", description = "Transport and cold chain logistics operations")
public class LogisticsController {

    private final LogisticsService logisticsService;

    public LogisticsController(LogisticsService logisticsService) {
        this.logisticsService = logisticsService;
    }

    @PostMapping("/transport-requests")
    @PreAuthorize("hasAnyRole('INVENTORY_MANAGER')")
    @Operation(summary = "Create a transport request")
    public ResponseEntity<ApiResponse<TransportRequestResponse>> createTransportRequest(
            @Valid @RequestBody TransportRequestCreateRequest request) {
        TransportRequestResponse response = logisticsService.createTransportRequest(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Transport request created successfully"));
    }

    @GetMapping("/transport-requests/{id}")
    @PreAuthorize("hasAnyRole('INVENTORY_MANAGER')")
    @Operation(summary = "Get transport request by ID")
    public ResponseEntity<ApiResponse<TransportRequestResponse>> getTransportRequest(
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(logisticsService.getTransportRequest(id)));
    }

    @GetMapping("/transport-requests")
    @PreAuthorize("hasAnyRole('INVENTORY_MANAGER')")
    @Operation(summary = "List all transport requests")
    public ResponseEntity<ApiResponse<List<TransportRequestResponse>>> listTransportRequests() {
        return ResponseEntity.ok(ApiResponse.success(logisticsService.getTransportRequests()));
    }

    @PatchMapping("/transport-requests/{id}/status")
    @PreAuthorize("hasAnyRole('INVENTORY_MANAGER')")
    @Operation(summary = "Update transport request status")
    public ResponseEntity<ApiResponse<TransportRequestResponse>> updateTransportStatus(
            @PathVariable UUID id,
            @RequestParam TransportStatusEnum status) {
        TransportRequestResponse response = logisticsService.updateTransportStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success(response, "Transport status updated"));
    }

    @PostMapping("/cold-chain-logs")
    @PreAuthorize("hasAnyRole('INVENTORY_MANAGER')")
    @Operation(summary = "Log cold chain data")
    public ResponseEntity<ApiResponse<ColdChainLogResponse>> logColdChain(
            @Valid @RequestBody ColdChainLogCreateRequest request) {
        ColdChainLogResponse response = logisticsService.logColdChain(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Cold chain data logged successfully"));
    }

    @GetMapping("/cold-chain-logs/transport/{transportId}")
    @PreAuthorize("hasAnyRole('INVENTORY_MANAGER')")
    @Operation(summary = "Get cold chain logs for a transport request")
    public ResponseEntity<ApiResponse<List<ColdChainLogResponse>>> getColdChainLogs(
            @PathVariable UUID transportId) {
        return ResponseEntity.ok(ApiResponse.success(logisticsService.getColdChainLogs(transportId)));
    }

    @PostMapping("/transport-boxes")
    @PreAuthorize("hasAnyRole('INVENTORY_MANAGER')")
    @Operation(summary = "Create a transport box")
    public ResponseEntity<ApiResponse<TransportBoxResponse>> createTransportBox(
            @Valid @RequestBody TransportBoxCreateRequest request) {
        TransportBoxResponse response = logisticsService.createTransportBox(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Transport box created successfully"));
    }

    @PostMapping("/deliveries")
    @PreAuthorize("hasAnyRole('INVENTORY_MANAGER')")
    @Operation(summary = "Confirm a delivery")
    public ResponseEntity<ApiResponse<DeliveryConfirmationResponse>> confirmDelivery(
            @Valid @RequestBody DeliveryConfirmationCreateRequest request) {
        DeliveryConfirmationResponse response = logisticsService.confirmDelivery(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Delivery confirmed successfully"));
    }
}
