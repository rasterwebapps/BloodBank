package com.bloodbank.notificationservice.dto;

import com.bloodbank.notificationservice.enums.CampaignStatusEnum;
import com.bloodbank.notificationservice.enums.CampaignTypeEnum;
import com.bloodbank.notificationservice.enums.ChannelEnum;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record CampaignResponse(
        UUID id,
        UUID branchId,
        String campaignCode,
        String campaignName,
        CampaignTypeEnum campaignType,
        ChannelEnum channel,
        String targetAudience,
        String targetCriteria,
        UUID templateId,
        Instant scheduledAt,
        Instant startedAt,
        Instant completedAt,
        int totalRecipients,
        int sentCount,
        int deliveredCount,
        int failedCount,
        CampaignStatusEnum status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
