package com.bloodbank.inventoryservice.dto;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record UnitReservationCreateRequest(
    @NotNull UUID componentId,
    @NotNull String reservedFor,
    @NotNull Instant expiryDate,
    @NotNull String reservedBy,
    String notes,
    @NotNull UUID branchId
) {}
