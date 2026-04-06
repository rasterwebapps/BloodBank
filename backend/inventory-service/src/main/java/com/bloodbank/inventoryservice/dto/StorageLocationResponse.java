package com.bloodbank.inventoryservice.dto;

import com.bloodbank.inventoryservice.enums.StorageLocationStatusEnum;
import com.bloodbank.inventoryservice.enums.StorageLocationTypeEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record StorageLocationResponse(
    UUID id,
    String locationCode,
    String locationName,
    StorageLocationTypeEnum locationType,
    BigDecimal temperatureMin,
    BigDecimal temperatureMax,
    Integer capacity,
    int currentCount,
    StorageLocationStatusEnum status,
    UUID branchId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
