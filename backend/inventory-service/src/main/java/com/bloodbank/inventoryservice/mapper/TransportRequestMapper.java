package com.bloodbank.inventoryservice.mapper;

import com.bloodbank.inventoryservice.dto.TransportRequestCreateRequest;
import com.bloodbank.inventoryservice.dto.TransportRequestResponse;
import com.bloodbank.inventoryservice.entity.TransportRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TransportRequestMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "requestNumber", ignore = true)
    @Mapping(target = "pickupTime", ignore = true)
    @Mapping(target = "actualDeliveryTime", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    TransportRequest toEntity(TransportRequestCreateRequest request);

    TransportRequestResponse toResponse(TransportRequest entity);

    List<TransportRequestResponse> toResponseList(List<TransportRequest> entities);
}
