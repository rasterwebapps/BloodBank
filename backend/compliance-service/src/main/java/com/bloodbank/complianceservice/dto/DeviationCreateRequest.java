package com.bloodbank.complianceservice.dto;

import com.bloodbank.complianceservice.enums.DeviationCategoryEnum;
import com.bloodbank.complianceservice.enums.DeviationSeverityEnum;
import com.bloodbank.complianceservice.enums.DeviationTypeEnum;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record DeviationCreateRequest(
        @NotNull DeviationTypeEnum deviationType,
        @NotNull DeviationSeverityEnum severity,
        @NotNull DeviationCategoryEnum category,
        @NotBlank @Size(max = 200) String title,
        @NotBlank String description,
        String detectedBy,
        UUID sopReferenceId,
        @NotNull UUID branchId
) {}
