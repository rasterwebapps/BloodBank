package com.bloodbank.notificationservice.mapper;

import com.bloodbank.notificationservice.dto.NotificationTemplateCreateRequest;
import com.bloodbank.notificationservice.dto.NotificationTemplateResponse;
import com.bloodbank.notificationservice.entity.NotificationTemplate;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface NotificationTemplateMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "active", constant = "true")
    NotificationTemplate toEntity(NotificationTemplateCreateRequest request);

    NotificationTemplateResponse toResponse(NotificationTemplate template);

    List<NotificationTemplateResponse> toResponseList(List<NotificationTemplate> templates);
}
