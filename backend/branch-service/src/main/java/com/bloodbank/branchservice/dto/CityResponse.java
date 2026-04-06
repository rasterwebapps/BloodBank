package com.bloodbank.branchservice.dto;

import java.io.Serializable;
import java.util.UUID;

public record CityResponse(
    UUID id,
    UUID regionId,
    String regionName,
    String cityName,
    String postalCode,
    boolean isActive
) implements Serializable {}
