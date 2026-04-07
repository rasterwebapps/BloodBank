package com.bloodbank.transfusionservice.mapper;

import com.bloodbank.transfusionservice.dto.CrossMatchRequestCreateRequest;
import com.bloodbank.transfusionservice.dto.CrossMatchRequestResponse;
import com.bloodbank.transfusionservice.entity.CrossMatchRequest;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CrossMatchRequestMapper {
    CrossMatchRequest toEntity(CrossMatchRequestCreateRequest request);
    CrossMatchRequestResponse toResponse(CrossMatchRequest entity);
    List<CrossMatchRequestResponse> toResponseList(List<CrossMatchRequest> entities);
}
