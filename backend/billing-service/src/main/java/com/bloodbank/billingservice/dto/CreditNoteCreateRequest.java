package com.bloodbank.billingservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record CreditNoteCreateRequest(
        @NotNull UUID invoiceId,
        @NotNull @Positive BigDecimal amount,
        @NotNull String reason,
        String notes,
        @NotNull UUID branchId
) {}
