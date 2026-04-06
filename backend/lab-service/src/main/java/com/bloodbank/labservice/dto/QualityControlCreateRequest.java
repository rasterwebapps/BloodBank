package com.bloodbank.labservice.dto;

import com.bloodbank.labservice.enums.QcLevelEnum;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record QualityControlCreateRequest(
        @NotNull UUID instrumentId,
        @NotNull QcLevelEnum qcLevel,
        @NotBlank String testName,
        String expectedValue,
        @NotBlank String actualValue,
        boolean isWithinRange,
        String correctiveAction,
        @NotBlank String performedBy,
        @NotNull UUID branchId
) {}
