package com.bloodbank.hospitalservice.dto;

import com.bloodbank.hospitalservice.enums.FeedbackCategoryEnum;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record HospitalFeedbackResponse(
        UUID id,
        UUID hospitalId,
        UUID requestId,
        Instant feedbackDate,
        Integer rating,
        FeedbackCategoryEnum category,
        String comments,
        String response,
        String respondedBy,
        Instant respondedAt,
        UUID branchId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) implements Serializable {}
