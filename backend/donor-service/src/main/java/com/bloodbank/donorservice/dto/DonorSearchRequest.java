package com.bloodbank.donorservice.dto;

import com.bloodbank.common.model.enums.DonorStatusEnum;

import java.util.UUID;

public record DonorSearchRequest(
        String firstName,
        String lastName,
        String email,
        String phone,
        String nationalId,
        UUID bloodGroupId,
        DonorStatusEnum status
) {}
