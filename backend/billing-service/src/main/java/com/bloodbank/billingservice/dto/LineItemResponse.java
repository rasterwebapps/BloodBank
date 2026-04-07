package com.bloodbank.billingservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record LineItemResponse(
        UUID id,
        UUID invoiceId,
        UUID bloodIssueId,
        UUID rateId,
        String description,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal taxPercentage,
        BigDecimal taxAmount,
        BigDecimal discountAmount,
        BigDecimal lineTotal,
        UUID branchId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
