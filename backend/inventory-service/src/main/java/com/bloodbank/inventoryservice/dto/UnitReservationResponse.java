package com.bloodbank.inventoryservice.dto;

import com.bloodbank.inventoryservice.enums.ReservationStatusEnum;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record UnitReservationResponse(
    UUID id,
    UUID componentId,
    String reservedFor,
    Instant reservationDate,
    Instant expiryDate,
    ReservationStatusEnum status,
    String reservedBy,
    String notes,
    UUID branchId,
    LocalDateTime createdAt
) {}
