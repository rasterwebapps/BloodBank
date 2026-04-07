package com.bloodbank.notificationservice.dto;

import com.bloodbank.notificationservice.enums.CampaignTypeEnum;
import com.bloodbank.notificationservice.enums.ChannelEnum;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record CampaignCreateRequest(
        UUID branchId,
        @NotBlank String campaignCode,
        @NotBlank String campaignName,
        @NotNull CampaignTypeEnum campaignType,
        @NotNull ChannelEnum channel,
        String targetAudience,
        String targetCriteria,
        UUID templateId,
        Instant scheduledAt
) {}
