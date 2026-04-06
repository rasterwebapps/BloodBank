package com.bloodbank.inventoryservice.dto;

import com.bloodbank.inventoryservice.enums.LabelTypeEnum;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ComponentLabelCreateRequest(
    @NotNull UUID componentId,
    @NotNull LabelTypeEnum labelType,
    @NotNull String labelData,
    @NotNull String printedBy,
    @NotNull UUID branchId
) {}
