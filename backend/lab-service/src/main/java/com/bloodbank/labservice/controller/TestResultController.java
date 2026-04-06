package com.bloodbank.labservice.controller;

import com.bloodbank.common.dto.ApiResponse;
import com.bloodbank.labservice.dto.TestResultApprovalRequest;
import com.bloodbank.labservice.dto.TestResultCreateRequest;
import com.bloodbank.labservice.dto.TestResultResponse;
import com.bloodbank.labservice.service.TestResultService;

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
@RequestMapping("/api/v1/test-results")
public class TestResultController {

    private final TestResultService testResultService;

    public TestResultController(TestResultService testResultService) {
        this.testResultService = testResultService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('LAB_TECHNICIAN')")
    public ResponseEntity<ApiResponse<TestResultResponse>> createResult(
            @Valid @RequestBody TestResultCreateRequest request) {
        TestResultResponse response = testResultService.createResult(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Test result recorded successfully"));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('LAB_TECHNICIAN')")
    public ResponseEntity<ApiResponse<TestResultResponse>> approveResult(
            @PathVariable UUID id,
            @Valid @RequestBody TestResultApprovalRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                testResultService.approveResult(id, request), "Test result approved"));
    }

    @GetMapping("/order/{testOrderId}")
    @PreAuthorize("hasAnyRole('LAB_TECHNICIAN','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<List<TestResultResponse>>> getResultsByOrder(
            @PathVariable UUID testOrderId) {
        return ResponseEntity.ok(ApiResponse.success(testResultService.getResultsByOrderId(testOrderId)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('LAB_TECHNICIAN','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<TestResultResponse>> getResultById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(testResultService.getResultById(id)));
    }
}
