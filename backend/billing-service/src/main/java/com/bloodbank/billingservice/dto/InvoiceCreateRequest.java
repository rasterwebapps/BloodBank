package com.bloodbank.billingservice.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record InvoiceCreateRequest(
        @NotNull UUID hospitalId,
        LocalDate dueDate,
        String currency,
        String notes,
        @NotNull UUID branchId
) {}
