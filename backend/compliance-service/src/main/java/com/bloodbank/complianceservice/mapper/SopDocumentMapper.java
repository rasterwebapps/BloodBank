package com.bloodbank.complianceservice.mapper;

import com.bloodbank.complianceservice.dto.SopCreateRequest;
import com.bloodbank.complianceservice.dto.SopResponse;
import com.bloodbank.complianceservice.entity.SopDocument;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SopDocumentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "branchId", ignore = true)
    @Mapping(target = "approvedBy", ignore = true)
    @Mapping(target = "approvedAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    SopDocument toEntity(SopCreateRequest request);

    SopResponse toResponse(SopDocument entity);

    List<SopResponse> toResponseList(List<SopDocument> entities);
}
