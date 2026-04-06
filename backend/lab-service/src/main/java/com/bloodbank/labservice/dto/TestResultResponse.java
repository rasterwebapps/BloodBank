package com.bloodbank.labservice.dto;

import com.bloodbank.common.model.enums.TestResultEnum;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record TestResultResponse(
        UUID id,
        UUID testOrderId,
        String testName,
        String testMethod,
        String resultValue,
        TestResultEnum resultStatus,
        boolean isAbnormal,
        String unitOfMeasure,
        String referenceRange,
        UUID instrumentId,
        String testedBy,
        String verifiedBy,
        Instant testedAt,
        Instant verifiedAt,
        String notes,
        UUID branchId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
