package com.bloodbank.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
    int status,
    String title,
    String detail,
    String instance,
    LocalDateTime timestamp,
    List<ValidationError> errors
) {}
