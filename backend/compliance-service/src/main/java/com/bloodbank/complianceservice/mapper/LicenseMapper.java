package com.bloodbank.complianceservice.mapper;

import com.bloodbank.complianceservice.dto.LicenseCreateRequest;
import com.bloodbank.complianceservice.dto.LicenseResponse;
import com.bloodbank.complianceservice.entity.License;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LicenseMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "branchId", ignore = true)
    @Mapping(target = "status", ignore = true)
    License toEntity(LicenseCreateRequest request);

    LicenseResponse toResponse(License entity);

    List<LicenseResponse> toResponseList(List<License> entities);
}
