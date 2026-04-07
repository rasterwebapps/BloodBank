package com.bloodbank.billingservice.controller;

import com.bloodbank.common.dto.ApiResponse;
import com.bloodbank.billingservice.dto.RateCreateRequest;
import com.bloodbank.billingservice.dto.RateResponse;
import com.bloodbank.billingservice.service.RateService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/v1/rates")
public class RateController {

    private final RateService rateService;

    public RateController(RateService rateService) {
        this.rateService = rateService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('BILLING_CLERK','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<RateResponse>> createRate(
            @Valid @RequestBody RateCreateRequest request) {
        RateResponse response = rateService.createRate(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Rate created successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('BILLING_CLERK','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<RateResponse>> getRateById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(rateService.getRateById(id)));
    }

    @GetMapping("/branch/{branchId}")
    @PreAuthorize("hasAnyRole('BILLING_CLERK','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<List<RateResponse>>> getActiveRatesByBranch(
            @PathVariable UUID branchId) {
        return ResponseEntity.ok(ApiResponse.success(rateService.getActiveRatesByBranch(branchId)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('BILLING_CLERK','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<RateResponse>> updateRate(
            @PathVariable UUID id, @Valid @RequestBody RateCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                rateService.updateRate(id, request), "Rate updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('BILLING_CLERK','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deactivateRate(@PathVariable UUID id) {
        rateService.deactivateRate(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Rate deactivated successfully"));
    }
}
