package com.bloodbank.reportingservice.controller;

import com.bloodbank.common.dto.ApiResponse;
import com.bloodbank.reportingservice.dto.ChainOfCustodyCreateRequest;
import com.bloodbank.reportingservice.dto.ChainOfCustodyResponse;
import com.bloodbank.reportingservice.service.ChainOfCustodyService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/chain-of-custody")
@Tag(name = "Chain of Custody", description = "Vein-to-vein traceability")
public class ChainOfCustodyController {

    private final ChainOfCustodyService chainOfCustodyService;

    public ChainOfCustodyController(ChainOfCustodyService chainOfCustodyService) {
        this.chainOfCustodyService = chainOfCustodyService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('AUDITOR','REGIONAL_ADMIN','SUPER_ADMIN')")
    @Operation(summary = "Add a custody event")
    public ResponseEntity<ApiResponse<ChainOfCustodyResponse>> addEvent(
            @Valid @RequestBody ChainOfCustodyCreateRequest request) {
        ChainOfCustodyResponse response = chainOfCustodyService.addEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Custody event recorded successfully"));
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    @PreAuthorize("hasAnyRole('AUDITOR','REGIONAL_ADMIN','SUPER_ADMIN')")
    @Operation(summary = "Get custody chain by entity")
    public ResponseEntity<ApiResponse<List<ChainOfCustodyResponse>>> getByEntityId(
            @PathVariable String entityType, @PathVariable UUID entityId) {
        List<ChainOfCustodyResponse> response = chainOfCustodyService.getByEntityId(entityType, entityId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/full-chain/{entityId}")
    @PreAuthorize("hasAnyRole('AUDITOR','REGIONAL_ADMIN','SUPER_ADMIN')")
    @Operation(summary = "Get full custody chain for a blood unit")
    public ResponseEntity<ApiResponse<List<ChainOfCustodyResponse>>> getFullChain(
            @PathVariable UUID entityId) {
        List<ChainOfCustodyResponse> response = chainOfCustodyService.getFullChain(entityId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
