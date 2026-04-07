package com.bloodbank.billingservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record LineItemCreateRequest(
        @NotNull UUID invoiceId,
        UUID bloodIssueId,
        UUID rateId,
        String description,
        @Positive int quantity,
        @NotNull @Positive BigDecimal unitPrice,
        BigDecimal taxPercentage,
        BigDecimal discountAmount,
        @NotNull UUID branchId
) {}
