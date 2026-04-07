package com.bloodbank.billingservice.dto;

import com.bloodbank.billingservice.enums.CreditNoteStatusEnum;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record CreditNoteResponse(
        UUID id,
        UUID invoiceId,
        String creditNoteNumber,
        Instant creditDate,
        BigDecimal amount,
        String reason,
        CreditNoteStatusEnum status,
        UUID appliedToInvoice,
        String notes,
        UUID branchId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
