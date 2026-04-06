package com.bloodbank.inventoryservice.dto;

import com.bloodbank.inventoryservice.enums.DeliveryConditionEnum;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record DeliveryConfirmationResponse(
    UUID id,
    UUID transportRequestId,
    String receivedBy,
    Instant receivedAt,
    DeliveryConditionEnum conditionOnArrival,
    BigDecimal temperatureOnArrival,
    int unitsReceived,
    int unitsRejected,
    String rejectionReason,
    String signatureReference,
    String notes,
    UUID branchId,
    LocalDateTime createdAt
) {}
