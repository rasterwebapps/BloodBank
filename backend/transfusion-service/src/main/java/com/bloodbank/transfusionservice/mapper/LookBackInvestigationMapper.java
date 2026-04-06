package com.bloodbank.transfusionservice.mapper;

import com.bloodbank.transfusionservice.dto.LookBackInvestigationCreateRequest;
import com.bloodbank.transfusionservice.dto.LookBackInvestigationResponse;
import com.bloodbank.transfusionservice.entity.LookBackInvestigation;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LookBackInvestigationMapper {
    LookBackInvestigation toEntity(LookBackInvestigationCreateRequest request);
    LookBackInvestigationResponse toResponse(LookBackInvestigation entity);
    List<LookBackInvestigationResponse> toResponseList(List<LookBackInvestigation> entities);
}
