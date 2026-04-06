package com.bloodbank.labservice.controller;

import com.bloodbank.common.dto.ApiResponse;
import com.bloodbank.labservice.dto.LabInstrumentCreateRequest;
import com.bloodbank.labservice.dto.LabInstrumentResponse;
import com.bloodbank.labservice.enums.InstrumentStatusEnum;
import com.bloodbank.labservice.service.LabInstrumentService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/instruments")
public class LabInstrumentController {

    private final LabInstrumentService labInstrumentService;

    public LabInstrumentController(LabInstrumentService labInstrumentService) {
        this.labInstrumentService = labInstrumentService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('LAB_TECHNICIAN','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<LabInstrumentResponse>> createInstrument(
            @Valid @RequestBody LabInstrumentCreateRequest request) {
        LabInstrumentResponse response = labInstrumentService.createInstrument(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Instrument created successfully"));
    }

    @GetMapping("/branch/{branchId}")
    @PreAuthorize("hasAnyRole('LAB_TECHNICIAN','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<List<LabInstrumentResponse>>> getInstrumentsByBranch(
            @PathVariable UUID branchId) {
        return ResponseEntity.ok(ApiResponse.success(labInstrumentService.getInstrumentsByBranch(branchId)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('LAB_TECHNICIAN','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<LabInstrumentResponse>> getInstrumentById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(labInstrumentService.getInstrumentById(id)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('LAB_TECHNICIAN','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<LabInstrumentResponse>> updateInstrumentStatus(
            @PathVariable UUID id, @RequestParam InstrumentStatusEnum status) {
        return ResponseEntity.ok(ApiResponse.success(
                labInstrumentService.updateInstrumentStatus(id, status), "Instrument status updated"));
    }
}
