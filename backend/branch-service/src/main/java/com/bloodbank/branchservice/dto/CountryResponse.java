package com.bloodbank.branchservice.dto;

import java.io.Serializable;
import java.util.UUID;

public record CountryResponse(
    UUID id,
    String countryCode,
    String countryName,
    String phoneCode,
    boolean isActive
) implements Serializable {}
