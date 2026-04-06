package com.bloodbank.donorservice.dto;

import com.bloodbank.donorservice.enums.GenderEnum;

import java.time.LocalDate;
import java.util.UUID;

public record DonorUpdateRequest(
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
        String occupation
) {}
