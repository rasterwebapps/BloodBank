package com.bloodbank.reportingservice.controller;

import com.bloodbank.common.dto.ApiResponse;
import com.bloodbank.reportingservice.dto.DashboardWidgetCreateRequest;
import com.bloodbank.reportingservice.dto.DashboardWidgetResponse;
import com.bloodbank.reportingservice.service.DashboardService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/api/v1/dashboard-widgets")
@Tag(name = "Dashboard Widgets", description = "Dashboard widget management")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('AUDITOR','REGIONAL_ADMIN','SUPER_ADMIN')")
    @Operation(summary = "Create a dashboard widget")
    public ResponseEntity<ApiResponse<DashboardWidgetResponse>> create(
            @Valid @RequestBody DashboardWidgetCreateRequest request) {
        DashboardWidgetResponse response = dashboardService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Widget created successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('AUDITOR','REGIONAL_ADMIN','SUPER_ADMIN')")
    @Operation(summary = "Get a dashboard widget by ID")
    public ResponseEntity<ApiResponse<DashboardWidgetResponse>> getById(@PathVariable UUID id) {
        DashboardWidgetResponse response = dashboardService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/code/{widgetCode}")
    @PreAuthorize("hasAnyRole('AUDITOR','REGIONAL_ADMIN','SUPER_ADMIN')")
    @Operation(summary = "Get a dashboard widget by code")
    public ResponseEntity<ApiResponse<DashboardWidgetResponse>> getByCode(@PathVariable String widgetCode) {
        DashboardWidgetResponse response = dashboardService.getByCode(widgetCode);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('AUDITOR','REGIONAL_ADMIN','SUPER_ADMIN')")
    @Operation(summary = "Get all active dashboard widgets")
    public ResponseEntity<ApiResponse<List<DashboardWidgetResponse>>> getActiveWidgets() {
        List<DashboardWidgetResponse> response = dashboardService.getActiveWidgets();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('AUDITOR','REGIONAL_ADMIN','SUPER_ADMIN')")
    @Operation(summary = "Update a dashboard widget")
    public ResponseEntity<ApiResponse<DashboardWidgetResponse>> update(
            @PathVariable UUID id, @Valid @RequestBody DashboardWidgetCreateRequest request) {
        DashboardWidgetResponse response = dashboardService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Widget updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('AUDITOR','REGIONAL_ADMIN','SUPER_ADMIN')")
    @Operation(summary = "Delete a dashboard widget (soft delete)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        dashboardService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Widget deleted successfully"));
    }
}
