package com.bloodbank.inventoryservice.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record ColdChainLogResponse(
    UUID id,
    UUID transportRequestId,
    UUID storageLocationId,
    UUID transportBoxId,
    BigDecimal temperature,
    BigDecimal humidity,
    Instant recordedAt,
    boolean isWithinRange,
    boolean alertTriggered,
    String recordedBy,
    String notes,
    UUID branchId,
    LocalDateTime createdAt
) {}
