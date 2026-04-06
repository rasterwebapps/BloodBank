package com.bloodbank.inventoryservice.mapper;

import com.bloodbank.inventoryservice.dto.UnitReservationCreateRequest;
import com.bloodbank.inventoryservice.dto.UnitReservationResponse;
import com.bloodbank.inventoryservice.entity.UnitReservation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UnitReservationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "reservationDate", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    UnitReservation toEntity(UnitReservationCreateRequest request);

    UnitReservationResponse toResponse(UnitReservation entity);

    List<UnitReservationResponse> toResponseList(List<UnitReservation> entities);
}
