package com.bloodbank.inventoryservice.dto;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record PooledComponentCreateRequest(
    @NotNull UUID componentTypeId,
    UUID bloodGroupId,
    @NotNull Integer totalVolumeMl,
    @NotNull Integer numberOfUnits,
    @NotNull Instant expiryDate,
    UUID storageLocationId,
    @NotNull String preparedBy,
    String notes,
    @NotNull UUID branchId
) {}
