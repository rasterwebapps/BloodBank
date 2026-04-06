package com.bloodbank.inventoryservice.mapper;

import com.bloodbank.inventoryservice.dto.BloodUnitCreateRequest;
import com.bloodbank.inventoryservice.dto.BloodUnitResponse;
import com.bloodbank.inventoryservice.entity.BloodUnit;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BloodUnitMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "unitNumber", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "ttiStatus", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(source = "expiresAt", target = "expiryDate")
    BloodUnit toEntity(BloodUnitCreateRequest request);

    BloodUnitResponse toResponse(BloodUnit entity);

    List<BloodUnitResponse> toResponseList(List<BloodUnit> entities);
}
