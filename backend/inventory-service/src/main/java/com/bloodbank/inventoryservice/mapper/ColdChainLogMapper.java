package com.bloodbank.inventoryservice.mapper;

import com.bloodbank.inventoryservice.dto.ColdChainLogCreateRequest;
import com.bloodbank.inventoryservice.dto.ColdChainLogResponse;
import com.bloodbank.inventoryservice.entity.ColdChainLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ColdChainLogMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "recordedAt", ignore = true)
    @Mapping(target = "withinRange", ignore = true)
    @Mapping(target = "alertTriggered", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    ColdChainLog toEntity(ColdChainLogCreateRequest request);

    @Mapping(source = "withinRange", target = "isWithinRange")
    ColdChainLogResponse toResponse(ColdChainLog entity);

    List<ColdChainLogResponse> toResponseList(List<ColdChainLog> entities);
}
