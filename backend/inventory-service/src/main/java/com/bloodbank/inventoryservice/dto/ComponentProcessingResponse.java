package com.bloodbank.inventoryservice.dto;

import com.bloodbank.inventoryservice.enums.ProcessResultEnum;
import com.bloodbank.inventoryservice.enums.ProcessTypeEnum;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record ComponentProcessingResponse(
    UUID id,
    UUID componentId,
    ProcessTypeEnum processType,
    Instant processDate,
    String processedBy,
    String equipmentUsed,
    String parameters,
    ProcessResultEnum result,
    String notes,
    UUID branchId,
    LocalDateTime createdAt
) {}
