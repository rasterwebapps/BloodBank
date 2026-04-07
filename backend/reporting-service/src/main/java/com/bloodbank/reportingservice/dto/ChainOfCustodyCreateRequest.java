package com.bloodbank.reportingservice.dto;

import com.bloodbank.reportingservice.enums.CustodyEventEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record ChainOfCustodyCreateRequest(
    @NotNull UUID branchId,
    @NotBlank String entityType,
    @NotNull UUID entityId,
    @NotNull CustodyEventEnum custodyEvent,
    String fromLocation,
    String toLocation,
    @NotBlank String handledBy,
    BigDecimal temperature,
    String notes
) {}
