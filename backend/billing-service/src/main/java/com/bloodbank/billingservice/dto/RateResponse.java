package com.bloodbank.billingservice.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record RateResponse(
        UUID id,
        UUID componentTypeId,
        String serviceCode,
        String serviceName,
        BigDecimal rateAmount,
        String currency,
        BigDecimal taxPercentage,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        boolean active,
        UUID branchId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
