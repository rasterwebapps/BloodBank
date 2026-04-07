package com.bloodbank.transfusionservice.dto;

import com.bloodbank.transfusionservice.enums.CrossMatchMethodEnum;
import com.bloodbank.transfusionservice.enums.CrossMatchResultEnum;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record CrossMatchResultResponse(
    UUID id,
    UUID crossmatchRequestId,
    UUID componentId,
    CrossMatchMethodEnum crossmatchMethod,
    CrossMatchResultEnum result,
    String performedBy,
    String verifiedBy,
    Instant performedAt,
    String notes,
    UUID branchId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
