package com.bloodbank.labservice.controller;

import com.bloodbank.common.dto.ApiResponse;
import com.bloodbank.common.dto.PagedResponse;
import com.bloodbank.labservice.dto.QualityControlCreateRequest;
import com.bloodbank.labservice.dto.QualityControlResponse;
import com.bloodbank.labservice.service.QualityControlService;

import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
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
@RequestMapping("/api/v1/qc")
public class QualityControlController {

    private final QualityControlService qualityControlService;

    public QualityControlController(QualityControlService qualityControlService) {
        this.qualityControlService = qualityControlService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('LAB_TECHNICIAN')")
    public ResponseEntity<ApiResponse<QualityControlResponse>> createRecord(
            @Valid @RequestBody QualityControlCreateRequest request) {
        QualityControlResponse response = qualityControlService.createRecord(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "QC record created successfully"));
    }

    @GetMapping("/instrument/{instrumentId}")
    @PreAuthorize("hasAnyRole('LAB_TECHNICIAN','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<List<QualityControlResponse>>> getRecordsByInstrument(
            @PathVariable UUID instrumentId) {
        return ResponseEntity.ok(ApiResponse.success(
                qualityControlService.getRecordsByInstrument(instrumentId)));
    }

    @GetMapping("/branch/{branchId}")
    @PreAuthorize("hasAnyRole('LAB_TECHNICIAN','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<PagedResponse<QualityControlResponse>>> getRecordsByBranch(
            @PathVariable UUID branchId, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                qualityControlService.getRecordsByBranch(branchId, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('LAB_TECHNICIAN','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<QualityControlResponse>> getRecordById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(qualityControlService.getRecordById(id)));
    }
}
