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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationMapper notificationMapper;

    @InjectMocks
    private NotificationService notificationService;

    private UUID notificationId;
    private UUID recipientId;
    private Notification notification;
    private NotificationResponse notificationResponse;
    private NotificationCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        notificationId = UUID.randomUUID();
        recipientId = UUID.randomUUID();

        notification = new Notification(ChannelEnum.EMAIL, "Test Subject", "Test Body",
                RecipientTypeEnum.DONOR, recipientId);
        notification.setId(notificationId);
        notification.setStatus(NotificationStatusEnum.PENDING);

        notificationResponse = new NotificationResponse(
                notificationId, null, null, RecipientTypeEnum.DONOR, recipientId,
                "test@example.com", null, ChannelEnum.EMAIL, "Test Subject", "Test Body",
                NotificationStatusEnum.PENDING, null, null, null, null, 0, null,
                LocalDateTime.now(), LocalDateTime.now()
        );

        createRequest = new NotificationCreateRequest(
                null, null, RecipientTypeEnum.DONOR, recipientId,
                "test@example.com", null, ChannelEnum.EMAIL, "Test Subject", "Test Body"
        );
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("should create notification successfully")
        void shouldCreateNotificationSuccessfully() {
            when(notificationMapper.toEntity(createRequest)).thenReturn(notification);
            when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
            when(notificationMapper.toResponse(notification)).thenReturn(notificationResponse);

            NotificationResponse result = notificationService.create(createRequest);

            assertThat(result).isNotNull();
            assertThat(result.channel()).isEqualTo(ChannelEnum.EMAIL);
            assertThat(result.subject()).isEqualTo("Test Subject");
            verify(notificationRepository).save(any(Notification.class));
        }
    }

    @Nested
    @DisplayName("getById")
    class GetById {

        @Test
        @DisplayName("should return notification when found")
        void shouldReturnNotificationWhenFound() {
            when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));
            when(notificationMapper.toResponse(notification)).thenReturn(notificationResponse);

            NotificationResponse result = notificationService.getById(notificationId);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(notificationId);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when not found")
        void shouldThrowResourceNotFoundWhenNotFound() {
            when(notificationRepository.findById(notificationId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> notificationService.getById(notificationId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getByRecipientId")
    class GetByRecipientId {

        @Test
        @DisplayName("should return notifications for recipient")
        void shouldReturnNotificationsForRecipient() {
            List<Notification> notifications = List.of(notification);
            List<NotificationResponse> responses = List.of(notificationResponse);
            when(notificationRepository.findByRecipientId(recipientId)).thenReturn(notifications);
            when(notificationMapper.toResponseList(notifications)).thenReturn(responses);

            List<NotificationResponse> result = notificationService.getByRecipientId(recipientId);

            assertThat(result).hasSize(1);
            verify(notificationRepository).findByRecipientId(recipientId);
        }
    }

    @Nested
    @DisplayName("markAsSent")
    class MarkAsSent {

        @Test
        @DisplayName("should mark notification as sent")
        void shouldMarkNotificationAsSent() {
            when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));
            when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
            when(notificationMapper.toResponse(notification)).thenReturn(notificationResponse);

            NotificationResponse result = notificationService.markAsSent(notificationId);

            assertThat(result).isNotNull();
            assertThat(notification.getStatus()).isEqualTo(NotificationStatusEnum.SENT);
            assertThat(notification.getSentAt()).isNotNull();
            verify(notificationRepository).save(notification);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when not found")
        void shouldThrowResourceNotFoundWhenNotFound() {
            when(notificationRepository.findById(notificationId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> notificationService.markAsSent(notificationId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("markAsRead")
    class MarkAsRead {

        @Test
        @DisplayName("should mark notification as read")
        void shouldMarkNotificationAsRead() {
            when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));
            when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
            when(notificationMapper.toResponse(notification)).thenReturn(notificationResponse);

            NotificationResponse result = notificationService.markAsRead(notificationId);

            assertThat(result).isNotNull();
            assertThat(notification.getStatus()).isEqualTo(NotificationStatusEnum.READ);
            assertThat(notification.getReadAt()).isNotNull();
            verify(notificationRepository).save(notification);
        }
    }

    @Nested
    @DisplayName("createSystemNotification")
    class CreateSystemNotification {

        @Test
        @DisplayName("should create system notification")
        void shouldCreateSystemNotification() {
            UUID branchId = UUID.randomUUID();
            when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
            when(notificationMapper.toResponse(notification)).thenReturn(notificationResponse);

            NotificationResponse result = notificationService.createSystemNotification(
                    "System Alert", "Test body", branchId);

            assertThat(result).isNotNull();
            verify(notificationRepository).save(any(Notification.class));
        }
    }
}
