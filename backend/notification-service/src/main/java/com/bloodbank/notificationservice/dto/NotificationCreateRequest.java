package com.bloodbank.notificationservice.dto;

import com.bloodbank.notificationservice.enums.ChannelEnum;
import com.bloodbank.notificationservice.enums.RecipientTypeEnum;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record NotificationCreateRequest(
        UUID branchId,
        UUID templateId,
        @NotNull RecipientTypeEnum recipientType,
        @NotNull UUID recipientId,
        String recipientEmail,
        String recipientPhone,
        @NotNull ChannelEnum channel,
        @NotBlank String subject,
        @NotBlank String body
) {}
