package com.bloodbank.transfusionservice.dto;

import com.bloodbank.transfusionservice.enums.CrossMatchMethodEnum;
import com.bloodbank.transfusionservice.enums.CrossMatchResultEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CrossMatchResultCreateRequest(
    @NotNull UUID crossmatchRequestId,
    @NotNull UUID componentId,
    @NotNull CrossMatchMethodEnum crossmatchMethod,
    @NotNull CrossMatchResultEnum result,
    @NotBlank String performedBy,
    String verifiedBy,
    String notes,
    @NotNull UUID branchId
) {}
