package com.bloodbank.inventoryservice.dto;

import com.bloodbank.common.model.enums.ComponentStatusEnum;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record BloodComponentResponse(
    UUID id,
    UUID bloodUnitId,
    UUID componentTypeId,
    String componentNumber,
    UUID bloodGroupId,
    Integer volumeMl,
    BigDecimal weightGrams,
    Instant preparationDate,
    Instant expiryDate,
    ComponentStatusEnum status,
    UUID storageLocationId,
    boolean irradiated,
    boolean leukoreduced,
    UUID branchId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
