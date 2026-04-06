package com.bloodbank.transfusionservice.dto;

import com.bloodbank.common.model.enums.TransfusionStatusEnum;
import com.bloodbank.transfusionservice.enums.TransfusionOutcomeEnum;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransfusionResponse(
    UUID id,
    UUID bloodIssueId,
    String patientName,
    String patientId,
    UUID hospitalId,
    Instant transfusionStart,
    Instant transfusionEnd,
    Integer volumeTransfusedMl,
    String administeredBy,
    String verifiedBy,
    String preVitalSigns,
    String postVitalSigns,
    TransfusionStatusEnum status,
    TransfusionOutcomeEnum outcome,
    String notes,
    UUID branchId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
