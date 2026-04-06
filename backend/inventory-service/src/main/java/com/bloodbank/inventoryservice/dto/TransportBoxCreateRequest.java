package com.bloodbank.inventoryservice.dto;

import com.bloodbank.inventoryservice.enums.TransportBoxTypeEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record TransportBoxCreateRequest(
    @NotBlank String boxCode,
    @NotNull TransportBoxTypeEnum boxType,
    @NotNull Integer capacity,
    String temperatureRange,
    @NotNull UUID branchId
) {}
