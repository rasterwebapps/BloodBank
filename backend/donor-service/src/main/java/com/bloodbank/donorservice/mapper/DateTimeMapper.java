package com.bloodbank.donorservice.mapper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Utility class used by MapStruct mappers to convert between Instant (entity)
 * and LocalDateTime (DTO) using UTC.
 */
public class DateTimeMapper {

    public LocalDateTime toLocalDateTime(Instant instant) {
        return instant == null ? null : LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    public Instant toInstant(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.toInstant(ZoneOffset.UTC);
    }
}
