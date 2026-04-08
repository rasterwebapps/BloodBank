package com.bloodbank.hospitalservice.dto;

import com.bloodbank.hospitalservice.enums.HospitalTypeEnum;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record HospitalCreateRequest(
        @NotBlank String hospitalName,
        @NotNull HospitalTypeEnum hospitalType,
        String addressLine1,
        String addressLine2,
        UUID cityId,
        String postalCode,
        String phone,
        @Email String email,
        String contactPerson,
        String licenseNumber,
        Integer bedCount,
        boolean hasBloodBank,
        @NotNull UUID branchId
) {}
