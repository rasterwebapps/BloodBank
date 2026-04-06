package com.bloodbank.labservice.dto;

import com.bloodbank.common.model.enums.TestResultEnum;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record TestResultCreateRequest(
        @NotNull UUID testOrderId,
        @NotBlank String testName,
        String testMethod,
        String resultValue,
        @NotNull TestResultEnum resultStatus,
        boolean isAbnormal,
        String unitOfMeasure,
        String referenceRange,
        UUID instrumentId,
        @NotBlank String testedBy,
        String notes,
        @NotNull UUID branchId
) {}
