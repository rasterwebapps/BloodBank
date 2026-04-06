package com.bloodbank.inventoryservice.mapper;

import com.bloodbank.inventoryservice.dto.TransportBoxCreateRequest;
import com.bloodbank.inventoryservice.dto.TransportBoxResponse;
import com.bloodbank.inventoryservice.entity.TransportBox;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TransportBoxMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "lastSanitized", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    TransportBox toEntity(TransportBoxCreateRequest request);

    TransportBoxResponse toResponse(TransportBox entity);

    List<TransportBoxResponse> toResponseList(List<TransportBox> entities);
}
