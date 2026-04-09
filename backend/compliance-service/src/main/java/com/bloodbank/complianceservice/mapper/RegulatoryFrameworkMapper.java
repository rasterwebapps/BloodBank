package com.bloodbank.complianceservice.mapper;

import com.bloodbank.complianceservice.dto.RegulatoryFrameworkCreateRequest;
import com.bloodbank.complianceservice.dto.RegulatoryFrameworkResponse;
import com.bloodbank.complianceservice.entity.RegulatoryFramework;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RegulatoryFrameworkMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "active", constant = "true")
    RegulatoryFramework toEntity(RegulatoryFrameworkCreateRequest request);

    RegulatoryFrameworkResponse toResponse(RegulatoryFramework entity);

    List<RegulatoryFrameworkResponse> toResponseList(List<RegulatoryFramework> entities);
}
