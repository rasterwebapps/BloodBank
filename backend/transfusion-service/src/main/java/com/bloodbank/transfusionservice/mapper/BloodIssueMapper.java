package com.bloodbank.transfusionservice.mapper;

import com.bloodbank.transfusionservice.dto.BloodIssueCreateRequest;
import com.bloodbank.transfusionservice.dto.BloodIssueResponse;
import com.bloodbank.transfusionservice.entity.BloodIssue;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BloodIssueMapper {
    BloodIssue toEntity(BloodIssueCreateRequest request);
    BloodIssueResponse toResponse(BloodIssue entity);
    List<BloodIssueResponse> toResponseList(List<BloodIssue> entities);
}
