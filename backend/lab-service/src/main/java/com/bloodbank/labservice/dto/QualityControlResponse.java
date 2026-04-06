package com.bloodbank.labservice.dto;

import com.bloodbank.labservice.enums.QcLevelEnum;
import com.bloodbank.labservice.enums.QcStatusEnum;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record QualityControlResponse(
        UUID id,
        UUID instrumentId,
        Instant qcDate,
        QcLevelEnum qcLevel,
        String testName,
        String expectedValue,
        String actualValue,
        boolean isWithinRange,
        String correctiveAction,
        String performedBy,
        QcStatusEnum status,
        UUID branchId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
