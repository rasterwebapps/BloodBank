package com.bloodbank.branchservice.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

public record ComponentTypeResponse(
    UUID id,
    String typeCode,
    String typeName,
    String description,
    int shelfLifeDays,
    BigDecimal storageTempMin,
    BigDecimal storageTempMax,
    boolean isActive
) implements Serializable {}
