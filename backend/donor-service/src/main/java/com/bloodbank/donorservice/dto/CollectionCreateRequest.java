package com.bloodbank.donorservice.dto;

import com.bloodbank.donorservice.enums.CollectionTypeEnum;
import com.bloodbank.donorservice.enums.DonationTypeEnum;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CollectionCreateRequest(
        @NotNull UUID donorId,
        UUID healthRecordId,
        @NotNull CollectionTypeEnum collectionType,
        DonationTypeEnum donationType,
        String bagType,
        String bagLotNumber,
        String phlebotomistId,
        String notes,
        @NotNull UUID branchId
) {}
