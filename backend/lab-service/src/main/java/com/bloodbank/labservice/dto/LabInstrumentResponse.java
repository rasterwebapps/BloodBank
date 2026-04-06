package com.bloodbank.labservice.dto;

import com.bloodbank.labservice.enums.InstrumentStatusEnum;
import com.bloodbank.labservice.enums.InstrumentTypeEnum;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record LabInstrumentResponse(
        UUID id,
        String instrumentCode,
        String instrumentName,
        InstrumentTypeEnum instrumentType,
        String manufacturer,
        String model,
        String serialNumber,
        LocalDate installationDate,
        LocalDate lastCalibrationDate,
        LocalDate nextCalibrationDate,
        InstrumentStatusEnum status,
        UUID branchId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
