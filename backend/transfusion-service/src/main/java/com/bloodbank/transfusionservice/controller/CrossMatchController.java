package com.bloodbank.transfusionservice.controller;

import com.bloodbank.common.dto.ApiResponse;
import com.bloodbank.common.dto.PagedResponse;
import com.bloodbank.common.model.enums.RequestStatusEnum;
import com.bloodbank.transfusionservice.dto.CrossMatchRequestCreateRequest;
import com.bloodbank.transfusionservice.dto.CrossMatchRequestResponse;
import com.bloodbank.transfusionservice.dto.CrossMatchResultCreateRequest;
import com.bloodbank.transfusionservice.dto.CrossMatchResultResponse;
import com.bloodbank.transfusionservice.service.CrossMatchService;

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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/crossmatch")
@Tag(name = "Cross-Match Management", description = "Cross-match request and result operations")
public class CrossMatchController {

    private final CrossMatchService crossMatchService;

    public CrossMatchController(CrossMatchService crossMatchService) {
        this.crossMatchService = crossMatchService;
    }

    @PostMapping("/requests")
    @PreAuthorize("hasAnyRole('DOCTOR','NURSE','BRANCH_MANAGER')")
    @Operation(summary = "Create a crossmatch request")
    public ResponseEntity<ApiResponse<CrossMatchRequestResponse>> createRequest(
            @Valid @RequestBody CrossMatchRequestCreateRequest request) {
        CrossMatchRequestResponse response = crossMatchService.createRequest(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Crossmatch request created successfully"));
    }

    @PostMapping("/results")
    @PreAuthorize("hasAnyRole('DOCTOR','NURSE','BRANCH_MANAGER')")
    @Operation(summary = "Add a crossmatch result")
    public ResponseEntity<ApiResponse<CrossMatchResultResponse>> addResult(
            @Valid @RequestBody CrossMatchResultCreateRequest request) {
        CrossMatchResultResponse response = crossMatchService.addResult(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Crossmatch result added successfully"));
    }

    @GetMapping("/requests/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR','NURSE','BRANCH_MANAGER')")
    @Operation(summary = "Get crossmatch request by ID")
    public ResponseEntity<ApiResponse<CrossMatchRequestResponse>> getRequestById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(crossMatchService.getRequestById(id)));
    }

    @GetMapping("/requests/{requestId}/results")
    @PreAuthorize("hasAnyRole('DOCTOR','NURSE','BRANCH_MANAGER')")
    @Operation(summary = "Get crossmatch results for a request")
    public ResponseEntity<ApiResponse<List<CrossMatchResultResponse>>> getResultsByRequestId(
            @PathVariable UUID requestId) {
        return ResponseEntity.ok(ApiResponse.success(crossMatchService.getResultsByRequestId(requestId)));
    }

    @GetMapping("/requests/status/{status}")
    @PreAuthorize("hasAnyRole('DOCTOR','NURSE','BRANCH_MANAGER')")
    @Operation(summary = "Get crossmatch requests by status")
    public ResponseEntity<ApiResponse<PagedResponse<CrossMatchRequestResponse>>> getRequestsByStatus(
            @PathVariable RequestStatusEnum status,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(crossMatchService.getRequestsByStatus(status, pageable)));
    }
}
