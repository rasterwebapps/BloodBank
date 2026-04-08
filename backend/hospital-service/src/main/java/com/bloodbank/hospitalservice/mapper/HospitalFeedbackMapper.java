package com.bloodbank.hospitalservice.mapper;

import com.bloodbank.hospitalservice.dto.HospitalFeedbackCreateRequest;
import com.bloodbank.hospitalservice.dto.HospitalFeedbackResponse;
import com.bloodbank.hospitalservice.entity.HospitalFeedback;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface HospitalFeedbackMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "branchId", ignore = true)
    @Mapping(target = "feedbackDate", ignore = true)
    @Mapping(target = "response", ignore = true)
    @Mapping(target = "respondedBy", ignore = true)
    @Mapping(target = "respondedAt", ignore = true)
    HospitalFeedback toEntity(HospitalFeedbackCreateRequest request);

    HospitalFeedbackResponse toResponse(HospitalFeedback feedback);

    List<HospitalFeedbackResponse> toResponseList(List<HospitalFeedback> feedbackList);
}
