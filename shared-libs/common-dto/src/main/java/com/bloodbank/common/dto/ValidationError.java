package com.bloodbank.common.dto;

public record ValidationError(
    String field,
    String message,
    Object rejectedValue
) {}
