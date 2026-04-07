package com.bloodbank.documentservice.dto;

import com.bloodbank.documentservice.enums.DocumentTypeEnum;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record DocumentUploadRequest(
        @NotBlank @Size(max = 200) String documentName,
        @NotNull DocumentTypeEnum documentType,
        @Size(max = 100) String entityType,
        UUID entityId,
        @Size(max = 500) String description,
        @Size(max = 500) String tags,
        boolean confidential,
        UUID branchId
) {}
