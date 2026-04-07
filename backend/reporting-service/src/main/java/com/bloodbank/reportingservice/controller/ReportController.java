package com.bloodbank.reportingservice.controller;

import com.bloodbank.common.dto.ApiResponse;
import com.bloodbank.reportingservice.dto.ReportMetadataCreateRequest;
import com.bloodbank.reportingservice.dto.ReportMetadataResponse;
import com.bloodbank.reportingservice.dto.ReportScheduleCreateRequest;
import com.bloodbank.reportingservice.dto.ReportScheduleResponse;
import com.bloodbank.reportingservice.enums.ReportTypeEnum;
import com.bloodbank.reportingservice.service.ReportScheduleService;
import com.bloodbank.reportingservice.service.ReportService;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reports")
@Tag(name = "Reports", description = "Report metadata and schedule management")
public class ReportController {

    private final ReportService reportService;
    private final ReportScheduleService reportScheduleService;

    public ReportController(ReportService reportService, ReportScheduleService reportScheduleService) {
        this.reportService = reportService;
        this.reportScheduleService = reportScheduleService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('AUDITOR','REGIONAL_ADMIN','SUPER_ADMIN')")
    @Operation(summary = "Create report metadata")
    public ResponseEntity<ApiResponse<ReportMetadataResponse>> create(
            @Valid @RequestBody ReportMetadataCreateRequest request) {
        ReportMetadataResponse response = reportService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Report created successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('AUDITOR','REGIONAL_ADMIN','SUPER_ADMIN')")
    @Operation(summary = "Get report metadata by ID")
    public ResponseEntity<ApiResponse<ReportMetadataResponse>> getById(@PathVariable UUID id) {
        ReportMetadataResponse response = reportService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/code/{reportCode}")
    @PreAuthorize("hasAnyRole('AUDITOR','REGIONAL_ADMIN','SUPER_ADMIN')")
    @Operation(summary = "Get report metadata by code")
    public ResponseEntity<ApiResponse<ReportMetadataResponse>> getByCode(@PathVariable String reportCode) {
        ReportMetadataResponse response = reportService.getByCode(reportCode);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/type")
    @PreAuthorize("hasAnyRole('AUDITOR','REGIONAL_ADMIN','SUPER_ADMIN')")
    @Operation(summary = "Get reports by type")
    public ResponseEntity<ApiResponse<List<ReportMetadataResponse>>> getByType(
            @RequestParam ReportTypeEnum reportType) {
        List<ReportMetadataResponse> response = reportService.getByType(reportType);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('AUDITOR','REGIONAL_ADMIN','SUPER_ADMIN')")
    @Operation(summary = "Get all active reports")
    public ResponseEntity<ApiResponse<List<ReportMetadataResponse>>> getAllActive() {
        List<ReportMetadataResponse> response = reportService.getAllActive();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('AUDITOR','REGIONAL_ADMIN','SUPER_ADMIN')")
    @Operation(summary = "Update report metadata")
    public ResponseEntity<ApiResponse<ReportMetadataResponse>> update(
            @PathVariable UUID id, @Valid @RequestBody ReportMetadataCreateRequest request) {
        ReportMetadataResponse response = reportService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Report updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('AUDITOR','REGIONAL_ADMIN','SUPER_ADMIN')")
    @Operation(summary = "Delete report metadata (soft delete)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        reportService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Report deleted successfully"));
    }

    @PostMapping("/{reportId}/schedules")
    @PreAuthorize("hasAnyRole('AUDITOR','REGIONAL_ADMIN','SUPER_ADMIN')")
    @Operation(summary = "Create a schedule for a report")
    public ResponseEntity<ApiResponse<ReportScheduleResponse>> createSchedule(
            @PathVariable UUID reportId, @Valid @RequestBody ReportScheduleCreateRequest request) {
        ReportScheduleResponse response = reportScheduleService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Schedule created successfully"));
    }

    @GetMapping("/{reportId}/schedules")
    @PreAuthorize("hasAnyRole('AUDITOR','REGIONAL_ADMIN','SUPER_ADMIN')")
    @Operation(summary = "Get schedules for a report")
    public ResponseEntity<ApiResponse<List<ReportScheduleResponse>>> getSchedules(@PathVariable UUID reportId) {
        List<ReportScheduleResponse> response = reportScheduleService.getByReportId(reportId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/schedules/{scheduleId}")
    @PreAuthorize("hasAnyRole('AUDITOR','REGIONAL_ADMIN','SUPER_ADMIN')")
    @Operation(summary = "Update a report schedule")
    public ResponseEntity<ApiResponse<ReportScheduleResponse>> updateSchedule(
            @PathVariable UUID scheduleId, @Valid @RequestBody ReportScheduleCreateRequest request) {
        ReportScheduleResponse response = reportScheduleService.update(scheduleId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Schedule updated successfully"));
    }

    @DeleteMapping("/schedules/{scheduleId}")
    @PreAuthorize("hasAnyRole('AUDITOR','REGIONAL_ADMIN','SUPER_ADMIN')")
    @Operation(summary = "Delete a report schedule (soft delete)")
    public ResponseEntity<ApiResponse<Void>> deleteSchedule(@PathVariable UUID scheduleId) {
        reportScheduleService.delete(scheduleId);
        return ResponseEntity.ok(ApiResponse.success(null, "Schedule deleted successfully"));
    }
}
