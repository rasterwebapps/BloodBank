package com.bloodbank.inventoryservice.mapper;

import com.bloodbank.inventoryservice.dto.PooledComponentCreateRequest;
import com.bloodbank.inventoryservice.dto.PooledComponentResponse;
import com.bloodbank.inventoryservice.entity.PooledComponent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PooledComponentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "poolNumber", ignore = true)
    @Mapping(target = "preparationDate", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    PooledComponent toEntity(PooledComponentCreateRequest request);

    PooledComponentResponse toResponse(PooledComponent entity);

    List<PooledComponentResponse> toResponseList(List<PooledComponent> entities);
}
