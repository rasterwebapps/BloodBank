package com.bloodbank.reportingservice.dto;

import com.bloodbank.reportingservice.enums.ScheduleStatusEnum;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record ReportScheduleResponse(
    UUID id,
    UUID branchId,
    UUID reportId,
    String scheduleName,
    String cronExpression,
    String recipients,
    String parameters,
    boolean active,
    Instant lastRunAt,
    Instant nextRunAt,
    ScheduleStatusEnum lastRunStatus,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
