package com.bloodbank.requestmatchingservice.controller;

import com.bloodbank.common.dto.ApiResponse;
import com.bloodbank.common.dto.PagedResponse;
import com.bloodbank.requestmatchingservice.dto.EmergencyRequestCreateRequest;
import com.bloodbank.requestmatchingservice.dto.EmergencyRequestResponse;
import com.bloodbank.requestmatchingservice.enums.EmergencyStatusEnum;
import com.bloodbank.requestmatchingservice.service.EmergencyService;

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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/emergencies")
@Tag(name = "Emergency Management", description = "Emergency blood request workflow operations")
public class EmergencyController {

    private final EmergencyService emergencyService;

    public EmergencyController(EmergencyService emergencyService) {
        this.emergencyService = emergencyService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('DOCTOR','BRANCH_MANAGER')")
    @Operation(summary = "Create an emergency blood request")
    public ResponseEntity<ApiResponse<EmergencyRequestResponse>> createEmergencyRequest(
            @Valid @RequestBody EmergencyRequestCreateRequest request) {
        EmergencyRequestResponse response = emergencyService.createEmergencyRequest(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Emergency request created successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR','BRANCH_MANAGER')")
    @Operation(summary = "Get emergency request by ID")
    public ResponseEntity<ApiResponse<EmergencyRequestResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(emergencyService.getById(id)));
    }

    @GetMapping("/number/{requestNumber}")
    @PreAuthorize("hasAnyRole('DOCTOR','BRANCH_MANAGER')")
    @Operation(summary = "Get emergency request by request number")
    public ResponseEntity<ApiResponse<EmergencyRequestResponse>> getByRequestNumber(
            @PathVariable String requestNumber) {
        return ResponseEntity.ok(ApiResponse.success(emergencyService.getByRequestNumber(requestNumber)));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('DOCTOR','BRANCH_MANAGER')")
    @Operation(summary = "Get emergency requests by status")
    public ResponseEntity<ApiResponse<PagedResponse<EmergencyRequestResponse>>> getByStatus(
            @PathVariable EmergencyStatusEnum status,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(emergencyService.getByStatus(status, pageable)));
    }

    @PutMapping("/{id}/escalate")
    @PreAuthorize("hasAnyRole('DOCTOR','BRANCH_MANAGER')")
    @Operation(summary = "Escalate an emergency request priority")
    public ResponseEntity<ApiResponse<EmergencyRequestResponse>> escalate(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(emergencyService.escalate(id),
                "Emergency request escalated successfully"));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('DOCTOR','BRANCH_MANAGER')")
    @Operation(summary = "Cancel an emergency request")
    public ResponseEntity<ApiResponse<EmergencyRequestResponse>> cancel(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(emergencyService.cancel(id),
                "Emergency request cancelled"));
    }

    @PutMapping("/{id}/broadcast")
    @PreAuthorize("hasAnyRole('DOCTOR','BRANCH_MANAGER')")
    @Operation(summary = "Mark broadcast as sent for an emergency request")
    public ResponseEntity<ApiResponse<EmergencyRequestResponse>> markBroadcastSent(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(emergencyService.markBroadcastSent(id),
                "Broadcast marked as sent"));
    }

    @GetMapping("/hospital/{hospitalId}")
    @PreAuthorize("hasAnyRole('DOCTOR','BRANCH_MANAGER')")
    @Operation(summary = "Get emergency requests by hospital")
    public ResponseEntity<ApiResponse<List<EmergencyRequestResponse>>> getByHospital(
            @PathVariable UUID hospitalId) {
        return ResponseEntity.ok(ApiResponse.success(emergencyService.getByHospital(hospitalId)));
    }
}
