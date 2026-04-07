package com.bloodbank.documentservice.dto;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record DocumentVersionResponse(
        UUID id,
        UUID branchId,
        UUID documentId,
        int versionNumber,
        String storagePath,
        Long fileSizeBytes,
        String mimeType,
        String changeDescription,
        String uploadedBy,
        Instant uploadedAt,
        LocalDateTime createdAt
) {}
