package com.bloodbank.notificationservice.mapper;

import com.bloodbank.notificationservice.dto.NotificationCreateRequest;
import com.bloodbank.notificationservice.dto.NotificationResponse;
import com.bloodbank.notificationservice.entity.Notification;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "sentAt", ignore = true)
    @Mapping(target = "deliveredAt", ignore = true)
    @Mapping(target = "readAt", ignore = true)
    @Mapping(target = "failureReason", ignore = true)
    @Mapping(target = "retryCount", ignore = true)
    @Mapping(target = "externalReference", ignore = true)
    Notification toEntity(NotificationCreateRequest request);

    NotificationResponse toResponse(Notification notification);

    List<NotificationResponse> toResponseList(List<Notification> notifications);
}
