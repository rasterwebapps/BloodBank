package com.bloodbank.requestmatchingservice.controller;

import com.bloodbank.common.dto.ApiResponse;
import com.bloodbank.requestmatchingservice.dto.EmergencyRequestResponse;
import com.bloodbank.requestmatchingservice.service.RequestMatchingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/matching")
@Tag(name = "Request Matching", description = "Blood request matching operations")
public class RequestMatchingController {

    private final RequestMatchingService requestMatchingService;

    public RequestMatchingController(RequestMatchingService requestMatchingService) {
        this.requestMatchingService = requestMatchingService;
    }

    @PostMapping("/{requestId}")
    @PreAuthorize("hasAnyRole('DOCTOR','BRANCH_MANAGER')")
    @Operation(summary = "Match an emergency request to available inventory")
    public ResponseEntity<ApiResponse<EmergencyRequestResponse>> matchRequest(@PathVariable UUID requestId) {
        EmergencyRequestResponse response = requestMatchingService.matchRequest(requestId);
        return ResponseEntity.ok(ApiResponse.success(response, "Request matching attempted"));
    }

    @GetMapping("/open")
    @PreAuthorize("hasAnyRole('DOCTOR','BRANCH_MANAGER')")
    @Operation(summary = "Get all open emergency requests awaiting matching")
    public ResponseEntity<ApiResponse<List<EmergencyRequestResponse>>> getOpenRequests() {
        List<EmergencyRequestResponse> responses = requestMatchingService.getOpenRequests();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}
