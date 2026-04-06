package com.bloodbank.donorservice.dto;

import com.bloodbank.common.model.enums.CollectionStatusEnum;
import com.bloodbank.donorservice.enums.CollectionTypeEnum;
import com.bloodbank.donorservice.enums.DonationTypeEnum;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public record CollectionResponse(
        UUID id,
        UUID donorId,
        UUID healthRecordId,
        String collectionNumber,
        LocalDateTime collectionDate,
        CollectionTypeEnum collectionType,
        DonationTypeEnum donationType,
        Integer volumeMl,
        String bagType,
        String bagLotNumber,
        String phlebotomistId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        CollectionStatusEnum status,
        String notes,
        UUID campCollectionId,
        UUID branchId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) implements Serializable {}
