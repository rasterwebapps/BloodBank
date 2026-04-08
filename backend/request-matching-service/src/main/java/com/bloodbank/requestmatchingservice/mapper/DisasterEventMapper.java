package com.bloodbank.requestmatchingservice.mapper;

import com.bloodbank.requestmatchingservice.dto.DisasterEventCreateRequest;
import com.bloodbank.requestmatchingservice.dto.DisasterEventResponse;
import com.bloodbank.requestmatchingservice.entity.DisasterEvent;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DisasterEventMapper {
    DisasterEvent toEntity(DisasterEventCreateRequest request);
    DisasterEventResponse toResponse(DisasterEvent entity);
    List<DisasterEventResponse> toResponseList(List<DisasterEvent> entities);
}
