package com.bloodbank.notificationservice.dto;

import com.bloodbank.notificationservice.enums.ChannelEnum;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record NotificationTemplateCreateRequest(
        @NotBlank String templateCode,
        @NotBlank String templateName,
        @NotNull ChannelEnum channel,
        String subject,
        String bodyTemplate,
        String variables,
        String language
) {}
