package com.bloodbank.reportingservice.mapper;

import com.bloodbank.reportingservice.dto.ReportScheduleCreateRequest;
import com.bloodbank.reportingservice.dto.ReportScheduleResponse;
import com.bloodbank.reportingservice.entity.ReportSchedule;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReportScheduleMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "lastRunAt", ignore = true)
    @Mapping(target = "nextRunAt", ignore = true)
    @Mapping(target = "lastRunStatus", ignore = true)
    ReportSchedule toEntity(ReportScheduleCreateRequest request);

    ReportScheduleResponse toResponse(ReportSchedule reportSchedule);

    List<ReportScheduleResponse> toResponseList(List<ReportSchedule> reportSchedules);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "lastRunAt", ignore = true)
    @Mapping(target = "nextRunAt", ignore = true)
    @Mapping(target = "lastRunStatus", ignore = true)
    void updateEntity(ReportScheduleCreateRequest request, @MappingTarget ReportSchedule entity);
}
