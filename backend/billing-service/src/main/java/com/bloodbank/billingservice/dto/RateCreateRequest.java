package com.bloodbank.billingservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record RateCreateRequest(
        UUID componentTypeId,
        @NotBlank String serviceCode,
        @NotBlank String serviceName,
        @NotNull @Positive BigDecimal rateAmount,
        String currency,
        BigDecimal taxPercentage,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        @NotNull UUID branchId
) {}
