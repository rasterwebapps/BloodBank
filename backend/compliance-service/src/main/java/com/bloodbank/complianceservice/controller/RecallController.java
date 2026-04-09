package com.bloodbank.complianceservice.controller;

import com.bloodbank.common.dto.ApiResponse;
import com.bloodbank.complianceservice.dto.RecallCreateRequest;
import com.bloodbank.complianceservice.dto.RecallResponse;
import com.bloodbank.complianceservice.enums.RecallStatusEnum;
import com.bloodbank.complianceservice.enums.RecallTypeEnum;
import com.bloodbank.complianceservice.service.RecallService;

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
@RequestMapping("/api/v1/compliance/recalls")
public class RecallController {

    private final RecallService recallService;

    public RecallController(RecallService recallService) {
        this.recallService = recallService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('AUDITOR','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<RecallResponse>> create(
            @Valid @RequestBody RecallCreateRequest request) {
        RecallResponse response = recallService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Recall initiated successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('AUDITOR','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<RecallResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(recallService.getById(id)));
    }

    @GetMapping("/number/{number}")
    @PreAuthorize("hasAnyRole('AUDITOR','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<RecallResponse>> getByNumber(@PathVariable String number) {
        return ResponseEntity.ok(ApiResponse.success(recallService.getByRecallNumber(number)));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('AUDITOR','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<RecallResponse>>> getByStatus(@PathVariable RecallStatusEnum status) {
        return ResponseEntity.ok(ApiResponse.success(recallService.getByStatus(status)));
    }

    @GetMapping("/type/{type}")
    @PreAuthorize("hasAnyRole('AUDITOR','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<RecallResponse>>> getByType(@PathVariable RecallTypeEnum type) {
        return ResponseEntity.ok(ApiResponse.success(recallService.getByType(type)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('AUDITOR','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<RecallResponse>> updateStatus(
            @PathVariable UUID id, @RequestParam RecallStatusEnum status) {
        return ResponseEntity.ok(ApiResponse.success(
                recallService.updateStatus(id, status), "Recall status updated successfully"));
    }

    @PatchMapping("/{id}/close")
    @PreAuthorize("hasAnyRole('AUDITOR','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<RecallResponse>> close(
            @PathVariable UUID id, @RequestParam String closedBy) {
        return ResponseEntity.ok(ApiResponse.success(
                recallService.close(id, closedBy), "Recall closed successfully"));
    }
}
