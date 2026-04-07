package com.bloodbank.notificationservice.dto;

import com.bloodbank.notificationservice.enums.ChannelEnum;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationTemplateResponse(
        UUID id,
        String templateCode,
        String templateName,
        ChannelEnum channel,
        String subject,
        String bodyTemplate,
        String variables,
        String language,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
