package com.bloodbank.requestmatchingservice.mapper;

import com.bloodbank.requestmatchingservice.dto.DonorMobilizationCreateRequest;
import com.bloodbank.requestmatchingservice.dto.DonorMobilizationResponse;
import com.bloodbank.requestmatchingservice.entity.DonorMobilization;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DonorMobilizationMapper {
    DonorMobilization toEntity(DonorMobilizationCreateRequest request);
    DonorMobilizationResponse toResponse(DonorMobilization entity);
    List<DonorMobilizationResponse> toResponseList(List<DonorMobilization> entities);
}
