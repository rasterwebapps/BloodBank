package com.bloodbank.donorservice.mapper;

import com.bloodbank.donorservice.dto.DonorConsentCreateRequest;
import com.bloodbank.donorservice.dto.DonorConsentResponse;
import com.bloodbank.donorservice.entity.DonorConsent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = DateTimeMapper.class)
public interface DonorConsentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "branchId", ignore = true)
    @Mapping(target = "consentDate", ignore = true)
    @Mapping(target = "expiryDate", ignore = true)
    @Mapping(target = "revokedAt", ignore = true)
    DonorConsent toEntity(DonorConsentCreateRequest request);

    DonorConsentResponse toResponse(DonorConsent consent);

    List<DonorConsentResponse> toResponseList(List<DonorConsent> consents);
}
