package com.bloodbank.transfusionservice.mapper;

import com.bloodbank.transfusionservice.dto.HemovigilanceReportCreateRequest;
import com.bloodbank.transfusionservice.dto.HemovigilanceReportResponse;
import com.bloodbank.transfusionservice.entity.HemovigilanceReport;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface HemovigilanceReportMapper {
    HemovigilanceReport toEntity(HemovigilanceReportCreateRequest request);
    HemovigilanceReportResponse toResponse(HemovigilanceReport entity);
    List<HemovigilanceReportResponse> toResponseList(List<HemovigilanceReport> entities);
}
