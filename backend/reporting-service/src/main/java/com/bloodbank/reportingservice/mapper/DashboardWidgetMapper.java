package com.bloodbank.reportingservice.mapper;

import com.bloodbank.reportingservice.dto.DashboardWidgetCreateRequest;
import com.bloodbank.reportingservice.dto.DashboardWidgetResponse;
import com.bloodbank.reportingservice.entity.DashboardWidget;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DashboardWidgetMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "active", constant = "true")
    DashboardWidget toEntity(DashboardWidgetCreateRequest request);

    DashboardWidgetResponse toResponse(DashboardWidget dashboardWidget);

    List<DashboardWidgetResponse> toResponseList(List<DashboardWidget> dashboardWidgets);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "active", ignore = true)
    void updateEntity(DashboardWidgetCreateRequest request, @MappingTarget DashboardWidget entity);
}
