package com.bloodbank.reportingservice.dto;

import com.bloodbank.reportingservice.enums.CustodyEventEnum;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record ChainOfCustodyResponse(
    UUID id,
    UUID branchId,
    String entityType,
    UUID entityId,
    CustodyEventEnum custodyEvent,
    String fromLocation,
    String toLocation,
    String handledBy,
    BigDecimal temperature,
    Instant eventTime,
    String notes,
    LocalDateTime createdAt
) {}
