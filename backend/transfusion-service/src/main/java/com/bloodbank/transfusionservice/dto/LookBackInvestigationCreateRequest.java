package com.bloodbank.transfusionservice.dto;

import com.bloodbank.transfusionservice.enums.InfectionTypeEnum;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record LookBackInvestigationCreateRequest(
    @NotNull UUID donorId,
    UUID triggerTestResultId,
    @NotNull InfectionTypeEnum infectionType,
    @Min(0) int affectedUnitsCount,
    @Min(0) int recipientsTraced,
    @Min(0) int recipientsNotified,
    String findings,
    String correctiveActions,
    @NotNull UUID branchId
) {}
