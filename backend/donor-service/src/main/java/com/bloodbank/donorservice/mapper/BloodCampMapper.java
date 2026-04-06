package com.bloodbank.donorservice.mapper;

import com.bloodbank.donorservice.dto.BloodCampCreateRequest;
import com.bloodbank.donorservice.dto.BloodCampResponse;
import com.bloodbank.donorservice.dto.CampDonorResponse;
import com.bloodbank.donorservice.dto.CampResourceCreateRequest;
import com.bloodbank.donorservice.dto.CampResourceResponse;
import com.bloodbank.donorservice.entity.BloodCamp;
import com.bloodbank.donorservice.entity.CampDonor;
import com.bloodbank.donorservice.entity.CampResource;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = DateTimeMapper.class)
public interface BloodCampMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "branchId", ignore = true)
    @Mapping(target = "campCode", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "actualDonors", ignore = true)
    @Mapping(target = "totalCollected", ignore = true)
    @Mapping(target = "startTime", ignore = true)
    @Mapping(target = "endTime", ignore = true)
    BloodCamp toEntity(BloodCampCreateRequest request);

    BloodCampResponse toResponse(BloodCamp camp);

    List<BloodCampResponse> toResponseList(List<BloodCamp> camps);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "branchId", ignore = true)
    CampResource toEntity(CampResourceCreateRequest request);

    CampResourceResponse toResponse(CampResource resource);

    List<CampResourceResponse> toResourceResponseList(List<CampResource> resources);

    CampDonorResponse toResponse(CampDonor campDonor);

    List<CampDonorResponse> toCampDonorResponseList(List<CampDonor> campDonors);
}
