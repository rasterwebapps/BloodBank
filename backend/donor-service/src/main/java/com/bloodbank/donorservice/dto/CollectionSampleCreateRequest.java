package com.bloodbank.donorservice.dto;

import com.bloodbank.donorservice.enums.SampleTypeEnum;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CollectionSampleCreateRequest(
        @NotNull UUID collectionId,
        @NotNull SampleTypeEnum sampleType,
        String notes,
        @NotNull UUID branchId
) {}
