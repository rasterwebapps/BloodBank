package com.bloodbank.hospitalservice.dto;

import com.bloodbank.hospitalservice.enums.ContractStatusEnum;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record HospitalContractResponse(
        UUID id,
        UUID hospitalId,
        String contractNumber,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal discountPercentage,
        int paymentTermsDays,
        BigDecimal creditLimit,
        boolean autoRenew,
        ContractStatusEnum status,
        String termsDocumentUrl,
        String notes,
        UUID branchId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) implements Serializable {}
