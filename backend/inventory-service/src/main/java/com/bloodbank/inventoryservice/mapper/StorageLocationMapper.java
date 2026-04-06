package com.bloodbank.inventoryservice.mapper;

import com.bloodbank.inventoryservice.dto.StorageLocationCreateRequest;
import com.bloodbank.inventoryservice.dto.StorageLocationResponse;
import com.bloodbank.inventoryservice.entity.StorageLocation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StorageLocationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "currentCount", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    StorageLocation toEntity(StorageLocationCreateRequest request);

    StorageLocationResponse toResponse(StorageLocation entity);

    List<StorageLocationResponse> toResponseList(List<StorageLocation> entities);
}
