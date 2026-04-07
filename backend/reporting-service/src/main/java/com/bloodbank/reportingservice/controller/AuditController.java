package com.bloodbank.reportingservice.controller;

import com.bloodbank.common.dto.ApiResponse;
import com.bloodbank.reportingservice.dto.AuditLogCreateRequest;
import com.bloodbank.reportingservice.dto.AuditLogResponse;
import com.bloodbank.reportingservice.enums.AuditActionEnum;
import com.bloodbank.reportingservice.service.AuditService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/audit-logs")
@Tag(name = "Audit Logs", description = "Audit trail management")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('AUDITOR','REGIONAL_ADMIN','SUPER_ADMIN')")
    @Operation(summary = "Create an audit log entry")
    public ResponseEntity<ApiResponse<AuditLogResponse>> create(
            @Valid @RequestBody AuditLogCreateRequest request) {
        AuditLogResponse response = auditService.createAuditLog(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Audit log created successfully"));
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    @PreAuthorize("hasAnyRole('AUDITOR','REGIONAL_ADMIN','SUPER_ADMIN')")
    @Operation(summary = "Get audit logs by entity")
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getByEntityId(
            @PathVariable String entityType, @PathVariable UUID entityId) {
        List<AuditLogResponse> response = auditService.getByEntityId(entityType, entityId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/actor/{actorId}")
    @PreAuthorize("hasAnyRole('AUDITOR','REGIONAL_ADMIN','SUPER_ADMIN')")
    @Operation(summary = "Get audit logs by actor")
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getByActorId(
            @PathVariable String actorId) {
        List<AuditLogResponse> response = auditService.getByActorId(actorId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/action")
    @PreAuthorize("hasAnyRole('AUDITOR','REGIONAL_ADMIN','SUPER_ADMIN')")
    @Operation(summary = "Get audit logs by action type")
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getByAction(
            @RequestParam AuditActionEnum action) {
        List<AuditLogResponse> response = auditService.getByAction(action);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
