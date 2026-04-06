package com.bloodbank.inventoryservice.dto;

import com.bloodbank.inventoryservice.enums.StorageLocationTypeEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record StorageLocationCreateRequest(
    @NotBlank String locationCode,
    @NotBlank String locationName,
    @NotNull StorageLocationTypeEnum locationType,
    BigDecimal temperatureMin,
    BigDecimal temperatureMax,
    @NotNull Integer capacity,
    @NotNull UUID branchId
) {}
