package com.bloodbank.reportingservice.dto;

import com.bloodbank.reportingservice.enums.AuditActionEnum;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record AuditLogResponse(
    UUID id,
    UUID branchId,
    String entityType,
    UUID entityId,
    AuditActionEnum action,
    String actorId,
    String actorName,
    String actorRole,
    String actorIp,
    String oldValues,
    String newValues,
    String description,
    Instant timestamp,
    LocalDateTime createdAt
) {}
