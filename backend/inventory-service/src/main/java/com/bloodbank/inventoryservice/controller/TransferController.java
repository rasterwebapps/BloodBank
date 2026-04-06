package com.bloodbank.inventoryservice.controller;

import com.bloodbank.common.dto.ApiResponse;
import com.bloodbank.inventoryservice.dto.StockTransferCreateRequest;
import com.bloodbank.inventoryservice.dto.StockTransferResponse;
import com.bloodbank.inventoryservice.service.StockTransferService;
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
@RequestMapping("/api/v1/transfers")
@Tag(name = "Stock Transfer Management", description = "Inter-branch stock transfer operations")
public class TransferController {

    private final StockTransferService stockTransferService;

    public TransferController(StockTransferService stockTransferService) {
        this.stockTransferService = stockTransferService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('INVENTORY_MANAGER','BRANCH_MANAGER')")
    @Operation(summary = "Create a stock transfer request")
    public ResponseEntity<ApiResponse<StockTransferResponse>> create(
            @Valid @RequestBody StockTransferCreateRequest request) {
        StockTransferResponse response = stockTransferService.createTransfer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Transfer request created successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('INVENTORY_MANAGER','BRANCH_MANAGER')")
    @Operation(summary = "Get transfer by ID")
    public ResponseEntity<ApiResponse<StockTransferResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(stockTransferService.getById(id)));
    }

    @GetMapping("/branch/{branchId}")
    @PreAuthorize("hasAnyRole('INVENTORY_MANAGER','BRANCH_MANAGER')")
    @Operation(summary = "Get transfers by branch")
    public ResponseEntity<ApiResponse<List<StockTransferResponse>>> getByBranch(
            @PathVariable UUID branchId) {
        return ResponseEntity.ok(ApiResponse.success(stockTransferService.getByBranch(branchId)));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('BRANCH_MANAGER')")
    @Operation(summary = "Approve a stock transfer")
    public ResponseEntity<ApiResponse<StockTransferResponse>> approve(
            @PathVariable UUID id,
            @RequestParam String approvedBy) {
        StockTransferResponse response = stockTransferService.approveTransfer(id, approvedBy);
        return ResponseEntity.ok(ApiResponse.success(response, "Transfer approved successfully"));
    }

    @PatchMapping("/{id}/ship")
    @PreAuthorize("hasAnyRole('INVENTORY_MANAGER')")
    @Operation(summary = "Ship a stock transfer")
    public ResponseEntity<ApiResponse<StockTransferResponse>> ship(@PathVariable UUID id) {
        StockTransferResponse response = stockTransferService.shipTransfer(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Transfer shipped successfully"));
    }

    @PatchMapping("/{id}/receive")
    @PreAuthorize("hasAnyRole('INVENTORY_MANAGER','BRANCH_MANAGER')")
    @Operation(summary = "Receive a stock transfer")
    public ResponseEntity<ApiResponse<StockTransferResponse>> receive(@PathVariable UUID id) {
        StockTransferResponse response = stockTransferService.receiveTransfer(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Transfer received successfully"));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('INVENTORY_MANAGER','BRANCH_MANAGER')")
    @Operation(summary = "Cancel a stock transfer")
    public ResponseEntity<ApiResponse<StockTransferResponse>> cancel(@PathVariable UUID id) {
        StockTransferResponse response = stockTransferService.cancelTransfer(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Transfer cancelled successfully"));
    }
}
