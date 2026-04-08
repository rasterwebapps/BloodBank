package com.bloodbank.requestmatchingservice.mapper;

import com.bloodbank.requestmatchingservice.dto.EmergencyRequestCreateRequest;
import com.bloodbank.requestmatchingservice.dto.EmergencyRequestResponse;
import com.bloodbank.requestmatchingservice.entity.EmergencyRequest;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EmergencyRequestMapper {
    EmergencyRequest toEntity(EmergencyRequestCreateRequest request);
    EmergencyRequestResponse toResponse(EmergencyRequest entity);
    List<EmergencyRequestResponse> toResponseList(List<EmergencyRequest> entities);
}
