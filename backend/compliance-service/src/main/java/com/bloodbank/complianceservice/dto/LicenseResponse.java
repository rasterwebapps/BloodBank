package com.bloodbank.complianceservice.dto;

import com.bloodbank.complianceservice.enums.LicenseStatusEnum;
import com.bloodbank.complianceservice.enums.LicenseTypeEnum;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record LicenseResponse(
        UUID id,
        LicenseTypeEnum licenseType,
        String licenseNumber,
        String issuingAuthority,
        LocalDate issueDate,
        LocalDate expiryDate,
        LocalDate renewalDate,
        String scope,
        String documentUrl,
        LicenseStatusEnum status,
        UUID branchId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
