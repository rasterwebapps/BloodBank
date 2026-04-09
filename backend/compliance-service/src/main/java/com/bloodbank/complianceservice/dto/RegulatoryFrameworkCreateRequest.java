package com.bloodbank.complianceservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record RegulatoryFrameworkCreateRequest(
        @NotBlank @Size(max = 50) String frameworkCode,
        @NotBlank @Size(max = 200) String frameworkName,
        @Size(max = 200) String authorityName,
        UUID countryId,
        String description,
        LocalDate effectiveDate,
        @Size(max = 20) String versionNumber,
        @Size(max = 500) String documentUrl
) {}
