package com.bloodbank.billingservice.dto;

import com.bloodbank.billingservice.enums.PaymentMethodEnum;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentCreateRequest(
        @NotNull UUID invoiceId,
        @NotNull @Positive BigDecimal amount,
        String currency,
        @NotNull PaymentMethodEnum paymentMethod,
        String referenceNumber,
        String notes,
        @NotNull UUID branchId
) {}
