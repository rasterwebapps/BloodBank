package com.bloodbank.complianceservice.mapper;

import com.bloodbank.complianceservice.dto.DeviationCreateRequest;
import com.bloodbank.complianceservice.dto.DeviationResponse;
import com.bloodbank.complianceservice.entity.Deviation;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DeviationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "branchId", ignore = true)
    @Mapping(target = "deviationNumber", ignore = true)
    @Mapping(target = "detectedDate", ignore = true)
    @Mapping(target = "rootCause", ignore = true)
    @Mapping(target = "correctiveAction", ignore = true)
    @Mapping(target = "preventiveAction", ignore = true)
    @Mapping(target = "closureDate", ignore = true)
    @Mapping(target = "closedBy", ignore = true)
    @Mapping(target = "status", ignore = true)
    Deviation toEntity(DeviationCreateRequest request);

    DeviationResponse toResponse(Deviation entity);

    List<DeviationResponse> toResponseList(List<Deviation> entities);
}
