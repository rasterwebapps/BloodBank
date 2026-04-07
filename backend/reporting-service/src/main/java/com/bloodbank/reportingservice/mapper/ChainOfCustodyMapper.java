package com.bloodbank.reportingservice.mapper;

import com.bloodbank.reportingservice.dto.ChainOfCustodyCreateRequest;
import com.bloodbank.reportingservice.dto.ChainOfCustodyResponse;
import com.bloodbank.reportingservice.entity.ChainOfCustody;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ChainOfCustodyMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "eventTime", expression = "java(java.time.Instant.now())")
    ChainOfCustody toEntity(ChainOfCustodyCreateRequest request);

    ChainOfCustodyResponse toResponse(ChainOfCustody chainOfCustody);

    List<ChainOfCustodyResponse> toResponseList(List<ChainOfCustody> chainOfCustodyList);
}
