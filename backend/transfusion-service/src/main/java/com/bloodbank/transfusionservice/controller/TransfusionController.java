package com.bloodbank.transfusionservice.controller;

import com.bloodbank.common.dto.ApiResponse;
import com.bloodbank.common.dto.PagedResponse;
import com.bloodbank.transfusionservice.dto.TransfusionCompleteRequest;
import com.bloodbank.transfusionservice.dto.TransfusionCreateRequest;
import com.bloodbank.transfusionservice.dto.TransfusionReactionCreateRequest;
import com.bloodbank.transfusionservice.dto.TransfusionReactionResponse;
import com.bloodbank.transfusionservice.dto.TransfusionResponse;
import com.bloodbank.transfusionservice.service.TransfusionService;

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
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transfusions")
@Tag(name = "Transfusion Management", description = "Transfusion lifecycle operations")
public class TransfusionController {

    private final TransfusionService transfusionService;

    public TransfusionController(TransfusionService transfusionService) {
        this.transfusionService = transfusionService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('DOCTOR','NURSE')")
    @Operation(summary = "Start a transfusion")
    public ResponseEntity<ApiResponse<TransfusionResponse>> startTransfusion(
            @Valid @RequestBody TransfusionCreateRequest request) {
        TransfusionResponse response = transfusionService.startTransfusion(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Transfusion started successfully"));
    }

    @PutMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('DOCTOR','NURSE')")
    @Operation(summary = "Complete a transfusion")
    public ResponseEntity<ApiResponse<TransfusionResponse>> completeTransfusion(
            @PathVariable UUID id,
            @Valid @RequestBody TransfusionCompleteRequest request) {
        TransfusionResponse response = transfusionService.completeTransfusion(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Transfusion completed successfully"));
    }

    @PostMapping("/reactions")
    @PreAuthorize("hasAnyRole('DOCTOR','NURSE')")
    @Operation(summary = "Report a transfusion reaction")
    public ResponseEntity<ApiResponse<TransfusionReactionResponse>> reportReaction(
            @Valid @RequestBody TransfusionReactionCreateRequest request) {
        TransfusionReactionResponse response = transfusionService.reportReaction(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Transfusion reaction reported successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR','NURSE')")
    @Operation(summary = "Get transfusion by ID")
    public ResponseEntity<ApiResponse<TransfusionResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(transfusionService.getById(id)));
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('DOCTOR','NURSE')")
    @Operation(summary = "Get transfusions by patient ID")
    public ResponseEntity<ApiResponse<PagedResponse<TransfusionResponse>>> getByPatient(
            @PathVariable String patientId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(transfusionService.getByPatient(patientId, pageable)));
    }
}
