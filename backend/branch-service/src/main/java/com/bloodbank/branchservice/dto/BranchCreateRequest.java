package com.bloodbank.branchservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record BranchCreateRequest(
    @NotBlank @Size(max = 20) String branchCode,
    @NotBlank @Size(max = 200) String branchName,
    @NotBlank String branchType,
    @NotBlank @Size(max = 255) String addressLine1,
    @Size(max = 255) String addressLine2,
    UUID cityId,
    @Size(max = 20) String postalCode,
    @Size(max = 20) String phone,
    @Email @Size(max = 255) String email,
    @Size(max = 100) String licenseNumber,
    LocalDate licenseExpiry,
    BigDecimal latitude,
    BigDecimal longitude,
    UUID parentBranchId
) {}
