package com.bloodbank.reportingservice.controller;

import com.bloodbank.common.dto.ApiResponse;
import com.bloodbank.reportingservice.dto.DigitalSignatureCreateRequest;
import com.bloodbank.reportingservice.dto.DigitalSignatureResponse;
import com.bloodbank.reportingservice.service.DigitalSignatureService;

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
@RequestMapping("/api/v1/digital-signatures")
@Tag(name = "Digital Signatures", description = "Digital signature management for FDA 21 CFR Part 11")
public class DigitalSignatureController {

    private final DigitalSignatureService digitalSignatureService;

    public DigitalSignatureController(DigitalSignatureService digitalSignatureService) {
        this.digitalSignatureService = digitalSignatureService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('AUDITOR','REGIONAL_ADMIN','SUPER_ADMIN')")
    @Operation(summary = "Create a digital signature")
    public ResponseEntity<ApiResponse<DigitalSignatureResponse>> create(
            @Valid @RequestBody DigitalSignatureCreateRequest request) {
        DigitalSignatureResponse response = digitalSignatureService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Digital signature created successfully"));
    }

    @GetMapping("/{id}/verify")
    @PreAuthorize("hasAnyRole('AUDITOR','REGIONAL_ADMIN','SUPER_ADMIN')")
    @Operation(summary = "Verify a digital signature")
    public ResponseEntity<ApiResponse<DigitalSignatureResponse>> verify(@PathVariable UUID id) {
        DigitalSignatureResponse response = digitalSignatureService.verify(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Signature is valid"));
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    @PreAuthorize("hasAnyRole('AUDITOR','REGIONAL_ADMIN','SUPER_ADMIN')")
    @Operation(summary = "Get digital signatures by entity")
    public ResponseEntity<ApiResponse<List<DigitalSignatureResponse>>> getByEntityId(
            @PathVariable String entityType, @PathVariable UUID entityId) {
        List<DigitalSignatureResponse> response = digitalSignatureService.getByEntityId(entityType, entityId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
