package com.bloodbank.hospitalservice.mapper;

import com.bloodbank.hospitalservice.dto.HospitalContractCreateRequest;
import com.bloodbank.hospitalservice.dto.HospitalContractResponse;
import com.bloodbank.hospitalservice.entity.HospitalContract;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface HospitalContractMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "branchId", ignore = true)
    @Mapping(target = "status", ignore = true)
    HospitalContract toEntity(HospitalContractCreateRequest request);

    HospitalContractResponse toResponse(HospitalContract contract);

    List<HospitalContractResponse> toResponseList(List<HospitalContract> contracts);
}
