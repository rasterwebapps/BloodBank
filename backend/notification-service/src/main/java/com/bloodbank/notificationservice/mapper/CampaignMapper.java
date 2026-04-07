package com.bloodbank.notificationservice.mapper;

import com.bloodbank.notificationservice.dto.CampaignCreateRequest;
import com.bloodbank.notificationservice.dto.CampaignResponse;
import com.bloodbank.notificationservice.entity.Campaign;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CampaignMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "startedAt", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    @Mapping(target = "totalRecipients", ignore = true)
    @Mapping(target = "sentCount", ignore = true)
    @Mapping(target = "deliveredCount", ignore = true)
    @Mapping(target = "failedCount", ignore = true)
    Campaign toEntity(CampaignCreateRequest request);

    CampaignResponse toResponse(Campaign campaign);

    List<CampaignResponse> toResponseList(List<Campaign> campaigns);
}
