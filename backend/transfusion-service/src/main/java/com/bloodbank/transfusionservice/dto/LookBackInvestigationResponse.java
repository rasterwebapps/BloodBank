package com.bloodbank.transfusionservice.dto;

import com.bloodbank.transfusionservice.enums.InfectionTypeEnum;
import com.bloodbank.transfusionservice.enums.LookBackStatusEnum;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record LookBackInvestigationResponse(
    UUID id,
    UUID donorId,
    UUID triggerTestResultId,
    String investigationNumber,
    Instant investigationDate,
    InfectionTypeEnum infectionType,
    int affectedUnitsCount,
    int recipientsTraced,
    int recipientsNotified,
    LookBackStatusEnum status,
    String findings,
    String correctiveActions,
    UUID branchId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
