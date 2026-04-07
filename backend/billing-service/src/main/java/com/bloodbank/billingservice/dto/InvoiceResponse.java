package com.bloodbank.billingservice.dto;

import com.bloodbank.billingservice.enums.InvoiceStatusEnum;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record InvoiceResponse(
        UUID id,
        UUID hospitalId,
        String invoiceNumber,
        Instant invoiceDate,
        LocalDate dueDate,
        BigDecimal subtotal,
        BigDecimal taxAmount,
        BigDecimal discountAmount,
        BigDecimal totalAmount,
        BigDecimal amountPaid,
        BigDecimal balanceDue,
        String currency,
        InvoiceStatusEnum status,
        String notes,
        UUID branchId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
