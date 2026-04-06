package com.bloodbank.donorservice.dto;

import com.bloodbank.donorservice.enums.CampStatusEnum;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record BloodCampResponse(
        UUID id,
        String campCode,
        String campName,
        String organizerName,
        String organizerContact,
        String venueName,
        String venueAddress,
        UUID cityId,
        BigDecimal latitude,
        BigDecimal longitude,
        LocalDate scheduledDate,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Integer expectedDonors,
        Integer actualDonors,
        int totalCollected,
        CampStatusEnum status,
        String coordinatorId,
        String notes,
        UUID branchId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) implements Serializable {}
