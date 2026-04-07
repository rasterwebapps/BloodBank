package com.bloodbank.notificationservice.mapper;

import com.bloodbank.notificationservice.dto.NotificationPreferenceCreateRequest;
import com.bloodbank.notificationservice.dto.NotificationPreferenceResponse;
import com.bloodbank.notificationservice.entity.NotificationPreference;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface NotificationPreferenceMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    NotificationPreference toEntity(NotificationPreferenceCreateRequest request);

    NotificationPreferenceResponse toResponse(NotificationPreference preference);

    List<NotificationPreferenceResponse> toResponseList(List<NotificationPreference> preferences);
}
