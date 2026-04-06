package com.bloodbank.inventoryservice.dto;

import com.bloodbank.common.model.enums.ComponentStatusEnum;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record PooledComponentResponse(
    UUID id,
    String poolNumber,
    UUID componentTypeId,
    UUID bloodGroupId,
    Integer totalVolumeMl,
    int numberOfUnits,
    Instant preparationDate,
    Instant expiryDate,
    ComponentStatusEnum status,
    UUID storageLocationId,
    String preparedBy,
    String notes,
    UUID branchId,
    LocalDateTime createdAt
) {}
