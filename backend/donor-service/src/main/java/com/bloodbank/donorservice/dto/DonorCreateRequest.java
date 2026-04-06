package com.bloodbank.donorservice.dto;

import com.bloodbank.donorservice.enums.DonorTypeEnum;
import com.bloodbank.donorservice.enums.GenderEnum;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record DonorCreateRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotNull LocalDate dateOfBirth,
        @NotNull GenderEnum gender,
        UUID bloodGroupId,
        String rhFactor,
        @Email String email,
        String phone,
        String addressLine1,
        String addressLine2,
        UUID cityId,
        String postalCode,
        String nationalId,
        String nationality,
        String occupation,
        DonorTypeEnum donorType,
        @NotNull UUID branchId
) {}
