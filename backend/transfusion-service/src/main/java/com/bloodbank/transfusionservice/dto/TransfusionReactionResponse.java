package com.bloodbank.transfusionservice.dto;

import com.bloodbank.common.model.enums.SeverityEnum;
import com.bloodbank.transfusionservice.enums.ReactionOutcomeEnum;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransfusionReactionResponse(
    UUID id,
    UUID transfusionId,
    UUID reactionTypeId,
    Instant onsetTime,
    String symptoms,
    SeverityEnum severity,
    String treatmentGiven,
    ReactionOutcomeEnum outcome,
    String reportedBy,
    UUID branchId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
