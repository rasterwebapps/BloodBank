package com.bloodbank.hospitalservice.dto;

import com.bloodbank.hospitalservice.enums.HospitalStatusEnum;
import com.bloodbank.hospitalservice.enums.HospitalTypeEnum;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public record HospitalResponse(
        UUID id,
        String hospitalCode,
        String hospitalName,
        HospitalTypeEnum hospitalType,
        String addressLine1,
        String addressLine2,
        UUID cityId,
        String postalCode,
        String phone,
        String email,
        String contactPerson,
        String licenseNumber,
        Integer bedCount,
        boolean hasBloodBank,
        HospitalStatusEnum status,
        UUID branchId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) implements Serializable {}
