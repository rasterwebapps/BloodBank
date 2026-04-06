package com.bloodbank.inventoryservice.mapper;

import com.bloodbank.inventoryservice.dto.ComponentProcessingCreateRequest;
import com.bloodbank.inventoryservice.dto.ComponentProcessingResponse;
import com.bloodbank.inventoryservice.entity.ComponentProcessing;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ComponentProcessingMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "processDate", ignore = true)
    @Mapping(target = "result", ignore = true)
    @Mapping(target = "notes", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    ComponentProcessing toEntity(ComponentProcessingCreateRequest request);

    ComponentProcessingResponse toResponse(ComponentProcessing entity);

    List<ComponentProcessingResponse> toResponseList(List<ComponentProcessing> entities);
}
