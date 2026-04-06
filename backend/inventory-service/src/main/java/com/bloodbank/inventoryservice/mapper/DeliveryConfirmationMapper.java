package com.bloodbank.inventoryservice.mapper;

import com.bloodbank.inventoryservice.dto.DeliveryConfirmationCreateRequest;
import com.bloodbank.inventoryservice.dto.DeliveryConfirmationResponse;
import com.bloodbank.inventoryservice.entity.DeliveryConfirmation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DeliveryConfirmationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "receivedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    DeliveryConfirmation toEntity(DeliveryConfirmationCreateRequest request);

    DeliveryConfirmationResponse toResponse(DeliveryConfirmation entity);

    List<DeliveryConfirmationResponse> toResponseList(List<DeliveryConfirmation> entities);
}
