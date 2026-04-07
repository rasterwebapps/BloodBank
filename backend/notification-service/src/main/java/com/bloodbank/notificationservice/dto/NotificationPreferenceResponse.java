package com.bloodbank.notificationservice.dto;

import com.bloodbank.notificationservice.enums.ChannelEnum;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

public record NotificationPreferenceResponse(
        UUID id,
        UUID userId,
        ChannelEnum channel,
        String eventType,
        boolean enabled,
        LocalTime quietHoursStart,
        LocalTime quietHoursEnd,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
