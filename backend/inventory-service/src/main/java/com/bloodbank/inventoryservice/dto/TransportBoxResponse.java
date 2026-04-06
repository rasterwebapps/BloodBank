package com.bloodbank.inventoryservice.dto;

import com.bloodbank.inventoryservice.enums.TransportBoxStatusEnum;
import com.bloodbank.inventoryservice.enums.TransportBoxTypeEnum;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransportBoxResponse(
    UUID id,
    String boxCode,
    TransportBoxTypeEnum boxType,
    Integer capacity,
    String temperatureRange,
    TransportBoxStatusEnum status,
    Instant lastSanitized,
    UUID branchId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
