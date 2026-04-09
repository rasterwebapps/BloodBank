package com.bloodbank.complianceservice.dto;

import com.bloodbank.complianceservice.enums.DeviationCategoryEnum;
import com.bloodbank.complianceservice.enums.DeviationSeverityEnum;
import com.bloodbank.complianceservice.enums.DeviationStatusEnum;
import com.bloodbank.complianceservice.enums.DeviationTypeEnum;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record DeviationResponse(
        UUID id,
        String deviationNumber,
        DeviationTypeEnum deviationType,
        DeviationSeverityEnum severity,
        DeviationCategoryEnum category,
        String title,
        String description,
        Instant detectedDate,
        String detectedBy,
        String rootCause,
        String correctiveAction,
        String preventiveAction,
        UUID sopReferenceId,
        Instant closureDate,
        String closedBy,
        DeviationStatusEnum status,
        UUID branchId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
