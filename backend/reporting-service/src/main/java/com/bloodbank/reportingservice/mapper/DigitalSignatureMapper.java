package com.bloodbank.reportingservice.mapper;

import com.bloodbank.reportingservice.dto.DigitalSignatureCreateRequest;
import com.bloodbank.reportingservice.dto.DigitalSignatureResponse;
import com.bloodbank.reportingservice.entity.DigitalSignature;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DigitalSignatureMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "signedAt", expression = "java(java.time.Instant.now())")
    @Mapping(target = "valid", constant = "true")
    DigitalSignature toEntity(DigitalSignatureCreateRequest request);

    DigitalSignatureResponse toResponse(DigitalSignature digitalSignature);

    List<DigitalSignatureResponse> toResponseList(List<DigitalSignature> digitalSignatures);
}
