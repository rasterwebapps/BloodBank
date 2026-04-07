package com.bloodbank.notificationservice.dto;

import com.bloodbank.notificationservice.enums.ChannelEnum;
import com.bloodbank.notificationservice.enums.NotificationStatusEnum;
import com.bloodbank.notificationservice.enums.RecipientTypeEnum;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        UUID branchId,
        UUID templateId,
        RecipientTypeEnum recipientType,
        UUID recipientId,
        String recipientEmail,
        String recipientPhone,
        ChannelEnum channel,
        String subject,
        String body,
        NotificationStatusEnum status,
        Instant sentAt,
        Instant deliveredAt,
        Instant readAt,
        String failureReason,
        int retryCount,
        String externalReference,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
