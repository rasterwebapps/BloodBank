package com.bloodbank.notificationservice.service;

import com.bloodbank.common.exceptions.ResourceNotFoundException;
import com.bloodbank.notificationservice.dto.NotificationPreferenceCreateRequest;
import com.bloodbank.notificationservice.dto.NotificationPreferenceResponse;
import com.bloodbank.notificationservice.entity.NotificationPreference;
import com.bloodbank.notificationservice.enums.ChannelEnum;
import com.bloodbank.notificationservice.mapper.NotificationPreferenceMapper;
import com.bloodbank.notificationservice.repository.NotificationPreferenceRepository;

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
class PreferenceServiceTest {

    @Mock
    private NotificationPreferenceRepository preferenceRepository;

    @Mock
    private NotificationPreferenceMapper preferenceMapper;

    @InjectMocks
    private PreferenceService preferenceService;

    private UUID preferenceId;
    private UUID userId;
    private NotificationPreference preference;
    private NotificationPreferenceResponse preferenceResponse;
    private NotificationPreferenceCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        preferenceId = UUID.randomUUID();
        userId = UUID.randomUUID();

        preference = new NotificationPreference(userId, ChannelEnum.EMAIL, "DONATION_COMPLETED");
        preference.setId(preferenceId);
        preference.setEnabled(true);

        preferenceResponse = new NotificationPreferenceResponse(
                preferenceId, userId, ChannelEnum.EMAIL, "DONATION_COMPLETED",
                true, null, null, LocalDateTime.now(), LocalDateTime.now()
        );

        createRequest = new NotificationPreferenceCreateRequest(
                userId, ChannelEnum.EMAIL, "DONATION_COMPLETED",
                true, null, null
        );
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("should create preference successfully")
        void shouldCreatePreferenceSuccessfully() {
            when(preferenceMapper.toEntity(createRequest)).thenReturn(preference);
            when(preferenceRepository.save(any(NotificationPreference.class))).thenReturn(preference);
            when(preferenceMapper.toResponse(preference)).thenReturn(preferenceResponse);

            NotificationPreferenceResponse result = preferenceService.create(createRequest);

            assertThat(result).isNotNull();
            assertThat(result.channel()).isEqualTo(ChannelEnum.EMAIL);
            assertThat(result.eventType()).isEqualTo("DONATION_COMPLETED");
            verify(preferenceRepository).save(any(NotificationPreference.class));
        }
    }

    @Nested
    @DisplayName("getById")
    class GetById {

        @Test
        @DisplayName("should return preference when found")
        void shouldReturnPreferenceWhenFound() {
            when(preferenceRepository.findById(preferenceId)).thenReturn(Optional.of(preference));
            when(preferenceMapper.toResponse(preference)).thenReturn(preferenceResponse);

            NotificationPreferenceResponse result = preferenceService.getById(preferenceId);

            assertThat(result).isNotNull();
            assertThat(result.userId()).isEqualTo(userId);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when not found")
        void shouldThrowResourceNotFoundWhenNotFound() {
            when(preferenceRepository.findById(preferenceId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> preferenceService.getById(preferenceId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getByUserId")
    class GetByUserId {

        @Test
        @DisplayName("should return preferences for user")
        void shouldReturnPreferencesForUser() {
            List<NotificationPreference> preferences = List.of(preference);
            List<NotificationPreferenceResponse> responses = List.of(preferenceResponse);
            when(preferenceRepository.findByUserId(userId)).thenReturn(preferences);
            when(preferenceMapper.toResponseList(preferences)).thenReturn(responses);

            List<NotificationPreferenceResponse> result = preferenceService.getByUserId(userId);

            assertThat(result).hasSize(1);
            verify(preferenceRepository).findByUserId(userId);
        }
    }

    @Nested
    @DisplayName("togglePreference")
    class TogglePreference {

        @Test
        @DisplayName("should toggle preference from enabled to disabled")
        void shouldTogglePreference() {
            when(preferenceRepository.findById(preferenceId)).thenReturn(Optional.of(preference));
            when(preferenceRepository.save(any(NotificationPreference.class))).thenReturn(preference);
            when(preferenceMapper.toResponse(preference)).thenReturn(preferenceResponse);

            NotificationPreferenceResponse result = preferenceService.togglePreference(preferenceId);

            assertThat(result).isNotNull();
            assertThat(preference.isEnabled()).isFalse();
            verify(preferenceRepository).save(preference);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when not found")
        void shouldThrowResourceNotFoundWhenNotFound() {
            when(preferenceRepository.findById(preferenceId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> preferenceService.togglePreference(preferenceId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
