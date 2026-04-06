package com.bloodbank.donorservice.dto;

import com.bloodbank.donorservice.enums.CampDonorStatusEnum;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public record CampDonorResponse(
        UUID id,
        UUID campId,
        UUID donorId,
        LocalDateTime registrationTime,
        CampDonorStatusEnum status,
        UUID branchId,
        LocalDateTime createdAt
) implements Serializable {}
