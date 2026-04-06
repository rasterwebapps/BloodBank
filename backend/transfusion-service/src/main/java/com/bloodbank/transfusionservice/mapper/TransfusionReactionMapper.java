package com.bloodbank.transfusionservice.mapper;

import com.bloodbank.transfusionservice.dto.TransfusionReactionCreateRequest;
import com.bloodbank.transfusionservice.dto.TransfusionReactionResponse;
import com.bloodbank.transfusionservice.entity.TransfusionReaction;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TransfusionReactionMapper {
    TransfusionReaction toEntity(TransfusionReactionCreateRequest request);
    TransfusionReactionResponse toResponse(TransfusionReaction entity);
    List<TransfusionReactionResponse> toResponseList(List<TransfusionReaction> entities);
}
