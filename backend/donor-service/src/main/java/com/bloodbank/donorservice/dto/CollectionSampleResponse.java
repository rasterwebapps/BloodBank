package com.bloodbank.donorservice.dto;

import com.bloodbank.donorservice.enums.SampleStatusEnum;
import com.bloodbank.donorservice.enums.SampleTypeEnum;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public record CollectionSampleResponse(
        UUID id,
        UUID collectionId,
        String sampleNumber,
        SampleTypeEnum sampleType,
        LocalDateTime collectedAt,
        SampleStatusEnum status,
        String notes,
        UUID branchId,
        LocalDateTime createdAt
) implements Serializable {}
