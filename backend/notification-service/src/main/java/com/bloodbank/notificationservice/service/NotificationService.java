package com.bloodbank.notificationservice.service;

import com.bloodbank.common.exceptions.ResourceNotFoundException;
import com.bloodbank.notificationservice.dto.NotificationCreateRequest;
import com.bloodbank.notificationservice.dto.NotificationResponse;
import com.bloodbank.notificationservice.entity.Notification;
import com.bloodbank.notificationservice.enums.ChannelEnum;
import com.bloodbank.notificationservice.enums.NotificationStatusEnum;
import com.bloodbank.notificationservice.enums.RecipientTypeEnum;
import com.bloodbank.notificationservice.mapper.NotificationMapper;
import com.bloodbank.notificationservice.repository.NotificationRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    public NotificationService(NotificationRepository notificationRepository,
                               NotificationMapper notificationMapper) {
        this.notificationRepository = notificationRepository;
        this.notificationMapper = notificationMapper;
    }

    @Transactional
    public NotificationResponse create(NotificationCreateRequest request) {
        log.info("Creating notification: channel={}, recipientType={}, recipientId={}",
                request.channel(), request.recipientType(), request.recipientId());
        Notification notification = notificationMapper.toEntity(request);
        notification.setStatus(NotificationStatusEnum.PENDING);
        notification.setRetryCount(0);
        notification = notificationRepository.save(notification);
        return notificationMapper.toResponse(notification);
    }

    public NotificationResponse getById(UUID id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", id));
        return notificationMapper.toResponse(notification);
    }

    public List<NotificationResponse> getByRecipientId(UUID recipientId) {
        return notificationMapper.toResponseList(
                notificationRepository.findByRecipientId(recipientId));
    }

    @Transactional
    public NotificationResponse markAsSent(UUID id) {
        log.info("Marking notification as sent: id={}", id);
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", id));
        notification.setStatus(NotificationStatusEnum.SENT);
        notification.setSentAt(Instant.now());
        notification = notificationRepository.save(notification);
        return notificationMapper.toResponse(notification);
    }

    @Transactional
    public NotificationResponse markAsRead(UUID id) {
        log.info("Marking notification as read: id={}", id);
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", id));
        notification.setStatus(NotificationStatusEnum.READ);
        notification.setReadAt(Instant.now());
        notification = notificationRepository.save(notification);
        return notificationMapper.toResponse(notification);
    }

    @Transactional
    public NotificationResponse createSystemNotification(String subject, String body, UUID branchId) {
        log.info("Creating system notification: subject={}", subject);
        Notification notification = new Notification(
                ChannelEnum.IN_APP, subject, body, RecipientTypeEnum.SYSTEM, null);
        notification.setBranchId(branchId);
        notification.setStatus(NotificationStatusEnum.PENDING);
        notification = notificationRepository.save(notification);
        return notificationMapper.toResponse(notification);
    }
}
