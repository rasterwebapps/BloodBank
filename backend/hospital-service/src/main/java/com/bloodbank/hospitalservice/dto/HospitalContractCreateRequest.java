package com.bloodbank.hospitalservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record HospitalContractCreateRequest(
        @NotNull UUID hospitalId,
        @NotBlank String contractNumber,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        @NotNull BigDecimal discountPercentage,
        int paymentTermsDays,
        BigDecimal creditLimit,
        boolean autoRenew,
        String termsDocumentUrl,
        String notes,
        @NotNull UUID branchId
) {}
