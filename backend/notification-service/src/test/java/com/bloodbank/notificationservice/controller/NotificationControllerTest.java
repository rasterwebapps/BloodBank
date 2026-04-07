package com.bloodbank.notificationservice.controller;

import com.bloodbank.common.exceptions.GlobalExceptionHandler;
import com.bloodbank.common.security.SecurityConfig;
import com.bloodbank.notificationservice.dto.NotificationCreateRequest;
import com.bloodbank.notificationservice.dto.NotificationResponse;
import com.bloodbank.notificationservice.enums.ChannelEnum;
import com.bloodbank.notificationservice.enums.NotificationStatusEnum;
import com.bloodbank.notificationservice.enums.RecipientTypeEnum;
import com.bloodbank.notificationservice.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = NotificationController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = GlobalExceptionHandler.class))
@Import(SecurityConfig.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private JwtDecoder jwtDecoder;

    private static final String BASE_URL = "/api/v1/notifications";
    private UUID notificationId;
    private UUID recipientId;
    private NotificationResponse sampleResponse;
    private NotificationCreateRequest sampleCreateRequest;

    @BeforeEach
    void setUp() {
        notificationId = UUID.randomUUID();
        recipientId = UUID.randomUUID();

        sampleResponse = new NotificationResponse(
                notificationId, null, null, RecipientTypeEnum.DONOR, recipientId,
                "test@example.com", null, ChannelEnum.EMAIL, "Test Subject", "Test Body",
                NotificationStatusEnum.PENDING, null, null, null, null, 0, null,
                LocalDateTime.now(), LocalDateTime.now()
        );

        sampleCreateRequest = new NotificationCreateRequest(
                null, null, RecipientTypeEnum.DONOR, recipientId,
                "test@example.com", null, ChannelEnum.EMAIL, "Test Subject", "Test Body"
        );
    }

    @Nested
    @DisplayName("POST /api/v1/notifications")
    class CreateNotification {

        @Test
        @DisplayName("should create notification as BRANCH_ADMIN — 201")
        @WithMockUser(roles = "BRANCH_ADMIN")
        void shouldCreateNotificationAsBranchAdmin() throws Exception {
            when(notificationService.create(any(NotificationCreateRequest.class))).thenReturn(sampleResponse);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.channel").value("EMAIL"));
        }

        @Test
        @DisplayName("should create notification as BRANCH_MANAGER — 201")
        @WithMockUser(roles = "BRANCH_MANAGER")
        void shouldCreateNotificationAsBranchManager() throws Exception {
            when(notificationService.create(any(NotificationCreateRequest.class))).thenReturn(sampleResponse);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("should reject as DONOR — 403")
        @WithMockUser(roles = "DONOR")
        void shouldRejectAsDonor() throws Exception {
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/notifications/{id}")
    class GetById {

        @Test
        @DisplayName("should return notification as BRANCH_ADMIN — 200")
        @WithMockUser(roles = "BRANCH_ADMIN")
        void shouldReturnNotificationAsBranchAdmin() throws Exception {
            when(notificationService.getById(notificationId)).thenReturn(sampleResponse);

            mockMvc.perform(get(BASE_URL + "/{id}", notificationId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(notificationId.toString()));
        }

        @Test
        @DisplayName("should reject as DONOR — 403")
        @WithMockUser(roles = "DONOR")
        void shouldRejectAsDonor() throws Exception {
            mockMvc.perform(get(BASE_URL + "/{id}", notificationId))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/notifications/recipient/{recipientId}")
    class GetByRecipientId {

        @Test
        @DisplayName("should return notifications for recipient as BRANCH_MANAGER — 200")
        @WithMockUser(roles = "BRANCH_MANAGER")
        void shouldReturnNotificationsForRecipient() throws Exception {
            when(notificationService.getByRecipientId(recipientId)).thenReturn(List.of(sampleResponse));

            mockMvc.perform(get(BASE_URL + "/recipient/{recipientId}", recipientId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/notifications/{id}/sent")
    class MarkAsSent {

        @Test
        @DisplayName("should mark as sent as BRANCH_ADMIN — 200")
        @WithMockUser(roles = "BRANCH_ADMIN")
        void shouldMarkAsSentAsBranchAdmin() throws Exception {
            when(notificationService.markAsSent(notificationId)).thenReturn(sampleResponse);

            mockMvc.perform(put(BASE_URL + "/{id}/sent", notificationId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/notifications/{id}/read")
    class MarkAsRead {

        @Test
        @DisplayName("should mark as read as BRANCH_ADMIN — 200")
        @WithMockUser(roles = "BRANCH_ADMIN")
        void shouldMarkAsReadAsBranchAdmin() throws Exception {
            when(notificationService.markAsRead(notificationId)).thenReturn(sampleResponse);

            mockMvc.perform(put(BASE_URL + "/{id}/read", notificationId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }
}
