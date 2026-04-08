package com.bloodbank.hospitalservice.controller;

import com.bloodbank.common.dto.ApiResponse;
import com.bloodbank.common.dto.PagedResponse;
import com.bloodbank.hospitalservice.dto.HospitalRequestCreateRequest;
import com.bloodbank.hospitalservice.dto.HospitalRequestResponse;
import com.bloodbank.hospitalservice.enums.HospitalRequestStatusEnum;
import com.bloodbank.hospitalservice.service.BloodRequestService;

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

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/hospital-requests")
@Tag(name = "Hospital Blood Request Management", description = "Submit and track blood requests from hospitals")
public class HospitalRequestController {

    private final BloodRequestService bloodRequestService;

    public HospitalRequestController(BloodRequestService bloodRequestService) {
        this.bloodRequestService = bloodRequestService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','BRANCH_ADMIN','BRANCH_MANAGER','DOCTOR','HOSPITAL_USER')")
    @Operation(summary = "Submit a new blood request")
    public ResponseEntity<ApiResponse<HospitalRequestResponse>> createRequest(
            @Valid @RequestBody HospitalRequestCreateRequest request) {
        HospitalRequestResponse response = bloodRequestService.createRequest(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Blood request submitted successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REGIONAL_ADMIN','AUDITOR','BRANCH_ADMIN','BRANCH_MANAGER','DOCTOR','INVENTORY_MANAGER','BILLING_CLERK','HOSPITAL_USER')")
    @Operation(summary = "Get blood request by ID")
    public ResponseEntity<ApiResponse<HospitalRequestResponse>> getRequestById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(bloodRequestService.getRequestById(id)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REGIONAL_ADMIN','AUDITOR','BRANCH_ADMIN','BRANCH_MANAGER','DOCTOR','INVENTORY_MANAGER','BILLING_CLERK','HOSPITAL_USER')")
    @Operation(summary = "List all blood requests")
    public ResponseEntity<ApiResponse<PagedResponse<HospitalRequestResponse>>> getAllRequests(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(bloodRequestService.getAllRequests(pageable)));
    }

    @GetMapping("/number/{requestNumber}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REGIONAL_ADMIN','AUDITOR','BRANCH_ADMIN','BRANCH_MANAGER','HOSPITAL_USER')")
    @Operation(summary = "Get blood request by request number")
    public ResponseEntity<ApiResponse<HospitalRequestResponse>> getRequestByNumber(
            @PathVariable String requestNumber) {
        return ResponseEntity.ok(ApiResponse.success(bloodRequestService.getRequestByNumber(requestNumber)));
    }

    @GetMapping("/hospital/{hospitalId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REGIONAL_ADMIN','AUDITOR','BRANCH_ADMIN','BRANCH_MANAGER','HOSPITAL_USER')")
    @Operation(summary = "Get blood requests by hospital")
    public ResponseEntity<ApiResponse<PagedResponse<HospitalRequestResponse>>> getRequestsByHospital(
            @PathVariable UUID hospitalId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(bloodRequestService.getRequestsByHospitalId(hospitalId, pageable)));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REGIONAL_ADMIN','AUDITOR','BRANCH_ADMIN','BRANCH_MANAGER','INVENTORY_MANAGER')")
    @Operation(summary = "Get blood requests by status")
    public ResponseEntity<ApiResponse<PagedResponse<HospitalRequestResponse>>> getRequestsByStatus(
            @PathVariable HospitalRequestStatusEnum status,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(bloodRequestService.getRequestsByStatus(status, pageable)));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','BRANCH_ADMIN','BRANCH_MANAGER')")
    @Operation(summary = "Update blood request status")
    public ResponseEntity<ApiResponse<HospitalRequestResponse>> updateRequestStatus(
            @PathVariable UUID id,
            @RequestParam HospitalRequestStatusEnum status,
            @RequestParam(required = false) String rejectionReason) {
        HospitalRequestResponse response = bloodRequestService.updateRequestStatus(id, status, rejectionReason);
        return ResponseEntity.ok(ApiResponse.success(response, "Request status updated successfully"));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','BRANCH_ADMIN','BRANCH_MANAGER','HOSPITAL_USER')")
    @Operation(summary = "Cancel a blood request")
    public ResponseEntity<ApiResponse<HospitalRequestResponse>> cancelRequest(@PathVariable UUID id) {
        HospitalRequestResponse response = bloodRequestService.cancelRequest(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Request cancelled successfully"));
    }
}
