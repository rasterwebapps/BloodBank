package com.bloodbank.complianceservice.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record RegulatoryFrameworkResponse(
        UUID id,
        String frameworkCode,
        String frameworkName,
        String authorityName,
        UUID countryId,
        String description,
        LocalDate effectiveDate,
        String versionNumber,
        String documentUrl,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
