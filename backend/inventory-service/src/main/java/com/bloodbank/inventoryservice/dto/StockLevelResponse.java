package com.bloodbank.inventoryservice.dto;

import java.util.UUID;

public record StockLevelResponse(
    UUID bloodGroupId,
    UUID componentTypeId,
    UUID branchId,
    long availableCount
) {}
