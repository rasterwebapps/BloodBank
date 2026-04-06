package com.bloodbank.donorservice.mapper;

import com.bloodbank.donorservice.dto.DonorDeferralCreateRequest;
import com.bloodbank.donorservice.dto.DonorDeferralResponse;
import com.bloodbank.donorservice.entity.DonorDeferral;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DonorDeferralMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "branchId", ignore = true)
    @Mapping(target = "deferralDate", ignore = true)
    @Mapping(target = "status", ignore = true)
    DonorDeferral toEntity(DonorDeferralCreateRequest request);

    DonorDeferralResponse toResponse(DonorDeferral deferral);

    List<DonorDeferralResponse> toResponseList(List<DonorDeferral> deferrals);
}
