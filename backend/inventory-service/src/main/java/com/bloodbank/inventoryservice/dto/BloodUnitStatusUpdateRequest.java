package com.bloodbank.inventoryservice.dto;

import com.bloodbank.common.model.enums.BloodUnitStatusEnum;
import jakarta.validation.constraints.NotNull;

public record BloodUnitStatusUpdateRequest(
    @NotNull BloodUnitStatusEnum status
) {}
