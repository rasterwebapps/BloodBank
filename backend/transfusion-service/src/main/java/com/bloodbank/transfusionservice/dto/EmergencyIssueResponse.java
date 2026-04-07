package com.bloodbank.transfusionservice.dto;

import com.bloodbank.transfusionservice.enums.CrossMatchResultEnum;
import com.bloodbank.transfusionservice.enums.EmergencyTypeEnum;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record EmergencyIssueResponse(
    UUID id,
    UUID bloodIssueId,
    EmergencyTypeEnum emergencyType,
    String authorizationBy,
    Instant authorizationTime,
    String clinicalJustification,
    boolean postCrossmatchDone,
    CrossMatchResultEnum postCrossmatchResult,
    UUID branchId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
