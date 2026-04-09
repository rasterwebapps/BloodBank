package com.bloodbank.complianceservice.dto;

import com.bloodbank.complianceservice.enums.SopCategoryEnum;
import com.bloodbank.complianceservice.enums.SopStatusEnum;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record SopResponse(
        UUID id,
        String sopCode,
        String sopTitle,
        SopCategoryEnum category,
        UUID frameworkId,
        String versionNumber,
        LocalDate effectiveDate,
        LocalDate reviewDate,
        String approvedBy,
        Instant approvedAt,
        String documentUrl,
        SopStatusEnum status,
        UUID branchId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
