package com.bloodbank.reportingservice.mapper;

import com.bloodbank.reportingservice.dto.ReportMetadataCreateRequest;
import com.bloodbank.reportingservice.dto.ReportMetadataResponse;
import com.bloodbank.reportingservice.entity.ReportMetadata;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReportMetadataMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "active", constant = "true")
    ReportMetadata toEntity(ReportMetadataCreateRequest request);

    ReportMetadataResponse toResponse(ReportMetadata reportMetadata);

    List<ReportMetadataResponse> toResponseList(List<ReportMetadata> reportMetadataList);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "active", ignore = true)
    void updateEntity(ReportMetadataCreateRequest request, @MappingTarget ReportMetadata entity);
}
