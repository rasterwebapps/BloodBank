package com.bloodbank.inventoryservice.mapper;

import com.bloodbank.inventoryservice.dto.UnitDisposalCreateRequest;
import com.bloodbank.inventoryservice.dto.UnitDisposalResponse;
import com.bloodbank.inventoryservice.entity.UnitDisposal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UnitDisposalMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "disposalDate", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    UnitDisposal toEntity(UnitDisposalCreateRequest request);

    UnitDisposalResponse toResponse(UnitDisposal entity);

    List<UnitDisposalResponse> toResponseList(List<UnitDisposal> entities);
}
