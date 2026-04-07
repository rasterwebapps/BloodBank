package com.bloodbank.transfusionservice.mapper;

import com.bloodbank.transfusionservice.dto.EmergencyIssueResponse;
import com.bloodbank.transfusionservice.entity.EmergencyIssue;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EmergencyIssueMapper {
    EmergencyIssueResponse toResponse(EmergencyIssue entity);
    List<EmergencyIssueResponse> toResponseList(List<EmergencyIssue> entities);
}
