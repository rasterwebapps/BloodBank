package com.bloodbank.inventoryservice.dto;

import com.bloodbank.inventoryservice.enums.DisposalReasonEnum;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record UnitDisposalResponse(
    UUID id,
    UUID bloodUnitId,
    UUID componentId,
    DisposalReasonEnum disposalReason,
    Instant disposalDate,
    String disposedBy,
    String authorizationBy,
    String notes,
    UUID branchId,
    LocalDateTime createdAt
) {}
