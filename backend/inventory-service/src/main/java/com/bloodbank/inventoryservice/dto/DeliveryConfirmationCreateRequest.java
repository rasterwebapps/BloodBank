package com.bloodbank.inventoryservice.dto;

import com.bloodbank.inventoryservice.enums.DeliveryConditionEnum;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record DeliveryConfirmationCreateRequest(
    @NotNull UUID transportRequestId,
    @NotNull String receivedBy,
    @NotNull DeliveryConditionEnum conditionOnArrival,
    BigDecimal temperatureOnArrival,
    @NotNull Integer unitsReceived,
    Integer unitsRejected,
    String rejectionReason,
    String signatureReference,
    String notes,
    @NotNull UUID branchId
) {}
