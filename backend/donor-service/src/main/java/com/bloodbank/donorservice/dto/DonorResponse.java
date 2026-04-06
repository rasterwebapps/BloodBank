package com.bloodbank.donorservice.dto;

import com.bloodbank.common.model.enums.DonorStatusEnum;
import com.bloodbank.donorservice.enums.DonorTypeEnum;
import com.bloodbank.donorservice.enums.GenderEnum;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record DonorResponse(
        UUID id,
        String donorNumber,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        GenderEnum gender,
        UUID bloodGroupId,
        String rhFactor,
        String email,
        String phone,
        String addressLine1,
        String addressLine2,
        UUID cityId,
        String postalCode,
        String nationalId,
        String nationality,
        String occupation,
        DonorTypeEnum donorType,
        DonorStatusEnum status,
        LocalDate lastDonationDate,
        int totalDonations,
        LocalDate registrationDate,
        String photoUrl,
        UUID branchId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) implements Serializable {}
