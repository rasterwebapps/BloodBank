package com.bloodbank.reportingservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ReportScheduleCreateRequest(
    UUID branchId,
    @NotNull UUID reportId,
    @NotBlank String scheduleName,
    @NotBlank String cronExpression,
    String recipients,
    String parameters
) {}
