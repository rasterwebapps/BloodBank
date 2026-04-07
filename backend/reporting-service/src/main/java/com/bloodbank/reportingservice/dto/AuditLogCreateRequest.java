package com.bloodbank.reportingservice.dto;

import com.bloodbank.reportingservice.enums.AuditActionEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AuditLogCreateRequest(
    UUID branchId,
    @NotBlank String entityType,
    @NotNull UUID entityId,
    @NotNull AuditActionEnum action,
    @NotBlank String actorId,
    String actorName,
    String actorRole,
    String actorIp,
    String oldValues,
    String newValues,
    String description
) {}
