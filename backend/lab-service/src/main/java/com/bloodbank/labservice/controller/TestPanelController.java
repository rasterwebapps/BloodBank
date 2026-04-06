package com.bloodbank.labservice.controller;

import com.bloodbank.common.dto.ApiResponse;
import com.bloodbank.labservice.dto.TestPanelCreateRequest;
import com.bloodbank.labservice.dto.TestPanelResponse;
import com.bloodbank.labservice.service.TestPanelService;

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
@RequestMapping("/api/v1/test-panels")
public class TestPanelController {

    private final TestPanelService testPanelService;

    public TestPanelController(TestPanelService testPanelService) {
        this.testPanelService = testPanelService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('BRANCH_MANAGER','BRANCH_ADMIN')")
    public ResponseEntity<ApiResponse<TestPanelResponse>> createPanel(
            @Valid @RequestBody TestPanelCreateRequest request) {
        TestPanelResponse response = testPanelService.createPanel(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Test panel created successfully"));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('LAB_TECHNICIAN','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<List<TestPanelResponse>>> getActivePanels() {
        return ResponseEntity.ok(ApiResponse.success(testPanelService.getActivePanels()));
    }

    @GetMapping("/mandatory")
    @PreAuthorize("hasAnyRole('LAB_TECHNICIAN','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<List<TestPanelResponse>>> getMandatoryPanels() {
        return ResponseEntity.ok(ApiResponse.success(testPanelService.getMandatoryPanels()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('LAB_TECHNICIAN','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<TestPanelResponse>> getPanelById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(testPanelService.getPanelById(id)));
    }
}
