package com.bloodbank.inventoryservice.mapper;

import com.bloodbank.inventoryservice.dto.ComponentLabelCreateRequest;
import com.bloodbank.inventoryservice.dto.ComponentLabelResponse;
import com.bloodbank.inventoryservice.entity.ComponentLabel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ComponentLabelMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "printedAt", ignore = true)
    @Mapping(target = "reprintCount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    ComponentLabel toEntity(ComponentLabelCreateRequest request);

    ComponentLabelResponse toResponse(ComponentLabel entity);

    List<ComponentLabelResponse> toResponseList(List<ComponentLabel> entities);
}
