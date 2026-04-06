package com.bloodbank.donorservice.dto;

import com.bloodbank.common.model.enums.SeverityEnum;
import com.bloodbank.donorservice.enums.ReactionOutcomeEnum;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public record AdverseReactionResponse(
        UUID id,
        UUID collectionId,
        String reactionType,
        SeverityEnum severity,
        LocalDateTime onsetTime,
        String description,
        String treatmentGiven,
        ReactionOutcomeEnum outcome,
        String reportedBy,
        UUID branchId,
        LocalDateTime createdAt
) implements Serializable {}
