package com.bloodbank.inventoryservice.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record ColdChainLogCreateRequest(
    UUID transportRequestId,
    UUID storageLocationId,
    UUID transportBoxId,
    @NotNull BigDecimal temperature,
    BigDecimal humidity,
    @NotNull String recordedBy,
    String notes,
    @NotNull UUID branchId
) {}
