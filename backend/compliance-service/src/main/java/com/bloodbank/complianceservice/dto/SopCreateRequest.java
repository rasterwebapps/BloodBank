package com.bloodbank.complianceservice.dto;

import com.bloodbank.complianceservice.enums.SopCategoryEnum;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record SopCreateRequest(
        @NotBlank @Size(max = 50) String sopCode,
        @NotBlank @Size(max = 200) String sopTitle,
        @NotNull SopCategoryEnum category,
        UUID frameworkId,
        @Size(max = 20) String versionNumber,
        @NotNull LocalDate effectiveDate,
        LocalDate reviewDate,
        @Size(max = 500) String documentUrl,
        @NotNull UUID branchId
) {}
