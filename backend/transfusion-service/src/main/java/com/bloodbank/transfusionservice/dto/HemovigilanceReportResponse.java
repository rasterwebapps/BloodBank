package com.bloodbank.transfusionservice.dto;

import com.bloodbank.transfusionservice.enums.HemovigilanceStatusEnum;
import com.bloodbank.transfusionservice.enums.ImputabilityEnum;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record HemovigilanceReportResponse(
    UUID id,
    UUID transfusionReactionId,
    String reportNumber,
    Instant reportDate,
    ImputabilityEnum imputability,
    String reporterName,
    String reporterDesignation,
    String investigationSummary,
    String correctiveActions,
    boolean reportedToAuthority,
    Instant authorityReportDate,
    HemovigilanceStatusEnum status,
    UUID branchId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
