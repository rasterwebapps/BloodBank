package com.bloodbank.inventoryservice.mapper;

import com.bloodbank.inventoryservice.dto.BloodComponentCreateRequest;
import com.bloodbank.inventoryservice.dto.BloodComponentResponse;
import com.bloodbank.inventoryservice.entity.BloodComponent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BloodComponentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "componentNumber", ignore = true)
    @Mapping(target = "weightGrams", ignore = true)
    @Mapping(target = "preparationDate", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "irradiated", ignore = true)
    @Mapping(target = "leukoreduced", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    BloodComponent toEntity(BloodComponentCreateRequest request);

    BloodComponentResponse toResponse(BloodComponent entity);

    List<BloodComponentResponse> toResponseList(List<BloodComponent> entities);
}
