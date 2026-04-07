package com.bloodbank.notificationservice.dto;

import com.bloodbank.notificationservice.enums.ChannelEnum;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;
import java.util.UUID;

public record NotificationPreferenceCreateRequest(
        @NotNull UUID userId,
        @NotNull ChannelEnum channel,
        @NotBlank String eventType,
        boolean enabled,
        LocalTime quietHoursStart,
        LocalTime quietHoursEnd
) {}
