package com.bloodbank.inventoryservice.dto;

import com.bloodbank.common.model.enums.BloodUnitStatusEnum;
import com.bloodbank.inventoryservice.enums.TtiStatusEnum;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record BloodUnitResponse(
    UUID id,
    UUID collectionId,
    UUID donorId,
    String unitNumber,
    UUID bloodGroupId,
    String rhFactor,
    Integer volumeMl,
    Instant collectionDate,
    Instant expiryDate,
    BloodUnitStatusEnum status,
    TtiStatusEnum ttiStatus,
    UUID storageLocationId,
    UUID branchId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
