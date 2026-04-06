package com.bloodbank.labservice.dto;

import com.bloodbank.labservice.enums.InstrumentTypeEnum;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record LabInstrumentCreateRequest(
        @NotBlank String instrumentName,
        @NotNull InstrumentTypeEnum instrumentType,
        String manufacturer,
        String model,
        String serialNumber,
        LocalDate installationDate,
        LocalDate lastCalibrationDate,
        LocalDate nextCalibrationDate,
        @NotNull UUID branchId
) {}
