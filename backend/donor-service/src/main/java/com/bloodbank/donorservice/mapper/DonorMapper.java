package com.bloodbank.donorservice.mapper;

import com.bloodbank.donorservice.dto.DonorCreateRequest;
import com.bloodbank.donorservice.dto.DonorResponse;
import com.bloodbank.donorservice.entity.Donor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DonorMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "branchId", ignore = true)
    @Mapping(target = "donorNumber", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "totalDonations", ignore = true)
    @Mapping(target = "lastDonationDate", ignore = true)
    @Mapping(target = "registrationDate", ignore = true)
    @Mapping(target = "photoUrl", ignore = true)
    Donor toEntity(DonorCreateRequest request);

    DonorResponse toResponse(Donor donor);

    List<DonorResponse> toResponseList(List<Donor> donors);
}
