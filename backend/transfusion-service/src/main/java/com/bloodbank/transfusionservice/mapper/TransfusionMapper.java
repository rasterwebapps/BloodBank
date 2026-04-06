package com.bloodbank.transfusionservice.mapper;

import com.bloodbank.transfusionservice.dto.TransfusionCreateRequest;
import com.bloodbank.transfusionservice.dto.TransfusionResponse;
import com.bloodbank.transfusionservice.entity.Transfusion;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TransfusionMapper {
    Transfusion toEntity(TransfusionCreateRequest request);
    TransfusionResponse toResponse(Transfusion entity);
    List<TransfusionResponse> toResponseList(List<Transfusion> entities);
}
