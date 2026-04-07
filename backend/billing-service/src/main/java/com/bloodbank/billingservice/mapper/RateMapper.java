package com.bloodbank.billingservice.mapper;

import com.bloodbank.billingservice.dto.RateCreateRequest;
import com.bloodbank.billingservice.dto.RateResponse;
import com.bloodbank.billingservice.entity.RateMaster;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RateMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "branchId", ignore = true)
    @Mapping(target = "active", constant = "true")
    RateMaster toEntity(RateCreateRequest request);

    RateResponse toResponse(RateMaster rateMaster);

    List<RateResponse> toResponseList(List<RateMaster> rateMasters);
}
