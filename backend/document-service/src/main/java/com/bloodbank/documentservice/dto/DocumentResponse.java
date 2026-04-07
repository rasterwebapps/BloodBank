package com.bloodbank.documentservice.dto;

import com.bloodbank.documentservice.enums.DocumentStatusEnum;
import com.bloodbank.documentservice.enums.DocumentTypeEnum;

import java.time.LocalDateTime;
import java.util.UUID;

public record DocumentResponse(
        UUID id,
        UUID branchId,
        String documentCode,
        String documentName,
        DocumentTypeEnum documentType,
        String entityType,
        UUID entityId,
        String mimeType,
        Long fileSizeBytes,
        String storagePath,
        String storageBucket,
        String description,
        String tags,
        boolean confidential,
        String uploadedBy,
        int currentVersion,
        DocumentStatusEnum status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
