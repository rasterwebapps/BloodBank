package com.bloodbank.donorservice.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record BloodCampUpdateRequest(
        String campName,
        String organizerName,
        String organizerContact,
        String venueName,
        String venueAddress,
        UUID cityId,
        BigDecimal latitude,
        BigDecimal longitude,
        LocalDate scheduledDate,
        Integer expectedDonors,
        String coordinatorId,
        String notes
) {}
