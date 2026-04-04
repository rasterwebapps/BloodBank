package com.bloodbank.branchservice.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record BranchResponse(
    UUID id,
    String branchCode,
    String branchName,
    String branchType,
    String addressLine1,
    String addressLine2,
    UUID cityId,
    String cityName,
    String postalCode,
    String phone,
    String email,
    String licenseNumber,
    LocalDate licenseExpiry,
    BigDecimal latitude,
    BigDecimal longitude,
    String status,
    UUID parentBranchId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) implements Serializable {}
