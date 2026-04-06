package com.bloodbank.inventoryservice.dto;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record BloodComponentCreateRequest(
    @NotNull UUID bloodUnitId,
    @NotNull UUID componentTypeId,
    UUID bloodGroupId,
    @NotNull Integer volumeMl,
    @NotNull Instant expiryDate,
    UUID storageLocationId,
    @NotNull UUID branchId
) {}
