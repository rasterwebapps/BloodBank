package com.bloodbank.transfusionservice.dto;

import jakarta.validation.constraints.NotNull;

public record TransfusionCompleteRequest(
    @NotNull Integer volumeTransfusedMl,
    String postVitalSigns,
    String notes
) {}
