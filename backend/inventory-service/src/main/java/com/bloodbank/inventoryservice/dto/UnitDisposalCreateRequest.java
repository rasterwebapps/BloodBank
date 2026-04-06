package com.bloodbank.inventoryservice.dto;

import com.bloodbank.inventoryservice.enums.DisposalReasonEnum;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UnitDisposalCreateRequest(
    UUID bloodUnitId,
    UUID componentId,
    @NotNull DisposalReasonEnum disposalReason,
    @NotNull String disposedBy,
    @NotNull String authorizationBy,
    String notes,
    @NotNull UUID branchId
) {}
