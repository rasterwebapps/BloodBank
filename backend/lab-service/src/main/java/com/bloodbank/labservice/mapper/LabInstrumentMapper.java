package com.bloodbank.labservice.mapper;

import com.bloodbank.labservice.dto.LabInstrumentCreateRequest;
import com.bloodbank.labservice.dto.LabInstrumentResponse;
import com.bloodbank.labservice.entity.LabInstrument;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LabInstrumentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "branchId", ignore = true)
    @Mapping(target = "instrumentCode", ignore = true)
    @Mapping(target = "status", ignore = true)
    LabInstrument toEntity(LabInstrumentCreateRequest request);

    LabInstrumentResponse toResponse(LabInstrument instrument);

    List<LabInstrumentResponse> toResponseList(List<LabInstrument> instruments);
}
