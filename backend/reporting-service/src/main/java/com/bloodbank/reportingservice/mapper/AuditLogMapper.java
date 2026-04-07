package com.bloodbank.reportingservice.mapper;

import com.bloodbank.reportingservice.dto.AuditLogCreateRequest;
import com.bloodbank.reportingservice.dto.AuditLogResponse;
import com.bloodbank.reportingservice.entity.AuditLog;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AuditLogMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "timestamp", expression = "java(java.time.Instant.now())")
    AuditLog toEntity(AuditLogCreateRequest request);

    AuditLogResponse toResponse(AuditLog auditLog);

    List<AuditLogResponse> toResponseList(List<AuditLog> auditLogs);
}
