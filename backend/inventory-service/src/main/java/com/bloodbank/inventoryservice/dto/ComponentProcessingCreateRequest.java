package com.bloodbank.inventoryservice.dto;

import com.bloodbank.inventoryservice.enums.ProcessTypeEnum;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ComponentProcessingCreateRequest(
    @NotNull UUID componentId,
    @NotNull ProcessTypeEnum processType,
    @NotNull String processedBy,
    String equipmentUsed,
    String parameters,
    @NotNull UUID branchId
) {}
