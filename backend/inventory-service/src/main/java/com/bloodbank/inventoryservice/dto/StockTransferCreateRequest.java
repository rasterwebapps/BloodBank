package com.bloodbank.inventoryservice.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record StockTransferCreateRequest(
    @NotNull UUID sourceBranchId,
    @NotNull UUID destinationBranchId,
    UUID componentId,
    UUID pooledComponentId,
    @NotNull String requestedBy,
    String notes,
    @NotNull UUID branchId
) {}
