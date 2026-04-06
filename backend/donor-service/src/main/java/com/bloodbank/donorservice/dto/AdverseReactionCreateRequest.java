package com.bloodbank.donorservice.dto;

import com.bloodbank.common.model.enums.SeverityEnum;
import com.bloodbank.donorservice.enums.ReactionOutcomeEnum;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record AdverseReactionCreateRequest(
        @NotNull UUID collectionId,
        @NotBlank String reactionType,
        @NotNull SeverityEnum severity,
        LocalDateTime onsetTime,
        String description,
        String treatmentGiven,
        ReactionOutcomeEnum outcome,
        String reportedBy,
        @NotNull UUID branchId
) {}
