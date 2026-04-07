package com.bloodbank.transfusionservice.mapper;

import com.bloodbank.transfusionservice.dto.CrossMatchResultCreateRequest;
import com.bloodbank.transfusionservice.dto.CrossMatchResultResponse;
import com.bloodbank.transfusionservice.entity.CrossMatchResult;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CrossMatchResultMapper {
    CrossMatchResult toEntity(CrossMatchResultCreateRequest request);
    CrossMatchResultResponse toResponse(CrossMatchResult entity);
    List<CrossMatchResultResponse> toResponseList(List<CrossMatchResult> entities);
}
