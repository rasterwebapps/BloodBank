package com.bloodbank.complianceservice.dto;

import com.bloodbank.complianceservice.enums.LicenseTypeEnum;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record LicenseCreateRequest(
        @NotNull LicenseTypeEnum licenseType,
        @NotBlank @Size(max = 100) String licenseNumber,
        @NotBlank @Size(max = 200) String issuingAuthority,
        @NotNull LocalDate issueDate,
        @NotNull LocalDate expiryDate,
        LocalDate renewalDate,
        String scope,
        @Size(max = 500) String documentUrl,
        @NotNull UUID branchId
) {}
