package com.bloodbank.hospitalservice.mapper;

import com.bloodbank.hospitalservice.dto.HospitalCreateRequest;
import com.bloodbank.hospitalservice.dto.HospitalResponse;
import com.bloodbank.hospitalservice.entity.Hospital;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface HospitalMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "branchId", ignore = true)
    @Mapping(target = "hospitalCode", ignore = true)
    @Mapping(target = "status", ignore = true)
    Hospital toEntity(HospitalCreateRequest request);

    HospitalResponse toResponse(Hospital hospital);

    List<HospitalResponse> toResponseList(List<Hospital> hospitals);
}
