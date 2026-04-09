package com.bloodbank.complianceservice.dto;

import com.bloodbank.complianceservice.enums.RecallSeverityEnum;
import com.bloodbank.complianceservice.enums.RecallStatusEnum;
import com.bloodbank.complianceservice.enums.RecallTypeEnum;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record RecallResponse(
        UUID id,
        String recallNumber,
        RecallTypeEnum recallType,
        String recallReason,
        RecallSeverityEnum severity,
        Instant initiatedDate,
        String initiatedBy,
        int affectedUnitsCount,
        int unitsRecovered,
        int unitsTransfused,
        boolean notificationSent,
        UUID lookbackInvestigationId,
        Instant closureDate,
        String closedBy,
        RecallStatusEnum status,
        String notes,
        UUID branchId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
