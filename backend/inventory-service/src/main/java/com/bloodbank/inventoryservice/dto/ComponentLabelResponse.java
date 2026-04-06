package com.bloodbank.inventoryservice.dto;

import com.bloodbank.inventoryservice.enums.LabelTypeEnum;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record ComponentLabelResponse(
    UUID id,
    UUID componentId,
    LabelTypeEnum labelType,
    String labelData,
    Instant printedAt,
    String printedBy,
    int reprintCount,
    UUID branchId,
    LocalDateTime createdAt
) {}
