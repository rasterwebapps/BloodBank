package com.bloodbank.inventoryservice.dto;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record BloodUnitCreateRequest(
    @NotNull UUID collectionId,
    @NotNull UUID donorId,
    @NotNull UUID bloodGroupId,
    @NotNull String rhFactor,
    @NotNull Integer volumeMl,
    @NotNull Instant collectionDate,
    @NotNull Instant expiresAt,
    UUID storageLocationId,
    @NotNull UUID branchId
) {}
