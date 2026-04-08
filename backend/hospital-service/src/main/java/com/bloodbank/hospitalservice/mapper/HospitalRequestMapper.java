package com.bloodbank.hospitalservice.mapper;

import com.bloodbank.hospitalservice.dto.HospitalRequestCreateRequest;
import com.bloodbank.hospitalservice.dto.HospitalRequestResponse;
import com.bloodbank.hospitalservice.entity.HospitalRequest;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface HospitalRequestMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "branchId", ignore = true)
    @Mapping(target = "requestNumber", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "unitsFulfilled", ignore = true)
    @Mapping(target = "rejectionReason", ignore = true)
    HospitalRequest toEntity(HospitalRequestCreateRequest request);

    HospitalRequestResponse toResponse(HospitalRequest request);

    List<HospitalRequestResponse> toResponseList(List<HospitalRequest> requests);
}
