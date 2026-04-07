package com.bloodbank.billingservice.dto;

import com.bloodbank.billingservice.enums.PaymentMethodEnum;
import com.bloodbank.billingservice.enums.PaymentStatusEnum;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        UUID invoiceId,
        String paymentNumber,
        Instant paymentDate,
        BigDecimal amount,
        String currency,
        PaymentMethodEnum paymentMethod,
        String referenceNumber,
        PaymentStatusEnum status,
        String notes,
        UUID branchId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
