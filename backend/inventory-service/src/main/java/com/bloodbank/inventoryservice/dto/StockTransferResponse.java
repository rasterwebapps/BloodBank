package com.bloodbank.inventoryservice.dto;

import com.bloodbank.inventoryservice.enums.TransferStatusEnum;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record StockTransferResponse(
    UUID id,
    String transferNumber,
    UUID sourceBranchId,
    UUID destinationBranchId,
    UUID componentId,
    UUID pooledComponentId,
    Instant requestDate,
    Instant shippedDate,
    Instant receivedDate,
    TransferStatusEnum status,
    String requestedBy,
    String approvedBy,
    String notes,
    UUID branchId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
