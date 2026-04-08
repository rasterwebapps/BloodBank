package com.bloodbank.requestmatchingservice.controller;

import com.bloodbank.common.dto.ApiResponse;
import com.bloodbank.common.dto.PagedResponse;
import com.bloodbank.requestmatchingservice.dto.DisasterEventCreateRequest;
import com.bloodbank.requestmatchingservice.dto.DisasterEventResponse;
import com.bloodbank.requestmatchingservice.dto.DonorMobilizationCreateRequest;
import com.bloodbank.requestmatchingservice.dto.DonorMobilizationResponse;
import com.bloodbank.requestmatchingservice.enums.DisasterStatusEnum;
import com.bloodbank.requestmatchingservice.enums.MobilizationStatusEnum;
import com.bloodbank.requestmatchingservice.service.DisasterResponseService;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/disasters")
@Tag(name = "Disaster Response", description = "Mass casualty protocol and donor mobilization operations")
public class DisasterResponseController {

    private final DisasterResponseService disasterResponseService;

    public DisasterResponseController(DisasterResponseService disasterResponseService) {
        this.disasterResponseService = disasterResponseService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REGIONAL_ADMIN')")
    @Operation(summary = "Create a disaster event")
    public ResponseEntity<ApiResponse<DisasterEventResponse>> createDisasterEvent(
            @Valid @RequestBody DisasterEventCreateRequest request) {
        DisasterEventResponse response = disasterResponseService.createDisasterEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Disaster event created successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REGIONAL_ADMIN')")
    @Operation(summary = "Get disaster event by ID")
    public ResponseEntity<ApiResponse<DisasterEventResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(disasterResponseService.getDisasterEventById(id)));
    }

    @GetMapping("/code/{eventCode}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REGIONAL_ADMIN')")
    @Operation(summary = "Get disaster event by event code")
    public ResponseEntity<ApiResponse<DisasterEventResponse>> getByEventCode(@PathVariable String eventCode) {
        return ResponseEntity.ok(ApiResponse.success(disasterResponseService.getByEventCode(eventCode)));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REGIONAL_ADMIN')")
    @Operation(summary = "Get disaster events by status")
    public ResponseEntity<ApiResponse<PagedResponse<DisasterEventResponse>>> getByStatus(
            @PathVariable DisasterStatusEnum status,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(disasterResponseService.getByStatus(status, pageable)));
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REGIONAL_ADMIN')")
    @Operation(summary = "Get all active disaster events")
    public ResponseEntity<ApiResponse<List<DisasterEventResponse>>> getActiveEvents() {
        return ResponseEntity.ok(ApiResponse.success(disasterResponseService.getActiveEvents()));
    }

    @PutMapping("/{id}/escalate")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REGIONAL_ADMIN')")
    @Operation(summary = "Escalate a disaster event severity")
    public ResponseEntity<ApiResponse<DisasterEventResponse>> escalate(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(disasterResponseService.escalateEvent(id),
                "Disaster event escalated successfully"));
    }

    @PutMapping("/{id}/close")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REGIONAL_ADMIN')")
    @Operation(summary = "Close a disaster event")
    public ResponseEntity<ApiResponse<DisasterEventResponse>> close(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(disasterResponseService.closeEvent(id),
                "Disaster event closed"));
    }

    // --- Donor Mobilization Endpoints ---

    @PostMapping("/{disasterEventId}/mobilizations")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REGIONAL_ADMIN')")
    @Operation(summary = "Mobilize a donor for a disaster event")
    public ResponseEntity<ApiResponse<DonorMobilizationResponse>> mobilizeDonor(
            @PathVariable UUID disasterEventId,
            @Valid @RequestBody DonorMobilizationCreateRequest request) {
        DonorMobilizationResponse response = disasterResponseService.mobilizeDonor(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Donor mobilized successfully"));
    }

    @GetMapping("/mobilizations/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REGIONAL_ADMIN')")
    @Operation(summary = "Get donor mobilization by ID")
    public ResponseEntity<ApiResponse<DonorMobilizationResponse>> getMobilizationById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(disasterResponseService.getMobilizationById(id)));
    }

    @GetMapping("/{disasterEventId}/mobilizations")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REGIONAL_ADMIN')")
    @Operation(summary = "Get mobilizations for a disaster event")
    public ResponseEntity<ApiResponse<PagedResponse<DonorMobilizationResponse>>> getMobilizationsByDisaster(
            @PathVariable UUID disasterEventId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                disasterResponseService.getMobilizationsByDisasterPaged(disasterEventId, pageable)));
    }

    @PutMapping("/mobilizations/{id}/response")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REGIONAL_ADMIN')")
    @Operation(summary = "Record a donor's response to mobilization")
    public ResponseEntity<ApiResponse<DonorMobilizationResponse>> recordResponse(
            @PathVariable UUID id,
            @RequestParam MobilizationStatusEnum response) {
        return ResponseEntity.ok(ApiResponse.success(
                disasterResponseService.recordResponse(id, response),
                "Donor response recorded"));
    }

    @PutMapping("/mobilizations/{id}/complete")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REGIONAL_ADMIN')")
    @Operation(summary = "Mark a mobilized donation as completed")
    public ResponseEntity<ApiResponse<DonorMobilizationResponse>> markDonationCompleted(
            @PathVariable UUID id,
            @RequestParam UUID collectionId) {
        return ResponseEntity.ok(ApiResponse.success(
                disasterResponseService.markDonationCompleted(id, collectionId),
                "Donation marked as completed"));
    }
}
