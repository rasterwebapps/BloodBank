package com.bloodbank.transfusionservice.dto;

import com.bloodbank.transfusionservice.enums.ImputabilityEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record HemovigilanceReportCreateRequest(
    @NotNull UUID transfusionReactionId,
    ImputabilityEnum imputability,
    @NotBlank String reporterName,
    String reporterDesignation,
    String investigationSummary,
    String correctiveActions,
    boolean reportedToAuthority,
    @NotNull UUID branchId
) {}
