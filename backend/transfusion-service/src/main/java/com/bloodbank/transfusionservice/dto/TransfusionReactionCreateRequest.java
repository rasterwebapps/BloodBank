package com.bloodbank.transfusionservice.dto;

import com.bloodbank.common.model.enums.SeverityEnum;
import com.bloodbank.transfusionservice.enums.ReactionOutcomeEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record TransfusionReactionCreateRequest(
    @NotNull UUID transfusionId,
    @NotNull UUID reactionTypeId,
    @NotNull Instant onsetTime,
    @NotBlank String symptoms,
    @NotNull SeverityEnum severity,
    String treatmentGiven,
    ReactionOutcomeEnum outcome,
    @NotBlank String reportedBy,
    @NotNull UUID branchId
) {}
