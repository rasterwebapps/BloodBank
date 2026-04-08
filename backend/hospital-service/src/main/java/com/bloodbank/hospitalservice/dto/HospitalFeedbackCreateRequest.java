package com.bloodbank.hospitalservice.dto;

import com.bloodbank.hospitalservice.enums.FeedbackCategoryEnum;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record HospitalFeedbackCreateRequest(
        @NotNull UUID hospitalId,
        UUID requestId,
        @Min(1) @Max(5) Integer rating,
        @NotNull FeedbackCategoryEnum category,
        String comments,
        @NotNull UUID branchId
) {}
