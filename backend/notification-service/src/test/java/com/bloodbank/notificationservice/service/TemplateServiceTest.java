package com.bloodbank.notificationservice.service;

import com.bloodbank.common.exceptions.ResourceNotFoundException;
import com.bloodbank.notificationservice.dto.NotificationTemplateCreateRequest;
import com.bloodbank.notificationservice.dto.NotificationTemplateResponse;
import com.bloodbank.notificationservice.entity.NotificationTemplate;
import com.bloodbank.notificationservice.enums.ChannelEnum;
import com.bloodbank.notificationservice.mapper.NotificationTemplateMapper;
import com.bloodbank.notificationservice.repository.NotificationTemplateRepository;

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
class TemplateServiceTest {

    @Mock
    private NotificationTemplateRepository templateRepository;

    @Mock
    private NotificationTemplateMapper templateMapper;

    @InjectMocks
    private TemplateService templateService;

    private UUID templateId;
    private NotificationTemplate template;
    private NotificationTemplateResponse templateResponse;
    private NotificationTemplateCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        templateId = UUID.randomUUID();

        template = new NotificationTemplate("DONATION_THANK_YOU", "Donation Thank You",
                ChannelEnum.EMAIL, "Thank you for your donation", "Dear {{name}}, thank you!");
        template.setId(templateId);
        template.setActive(true);

        templateResponse = new NotificationTemplateResponse(
                templateId, "DONATION_THANK_YOU", "Donation Thank You",
                ChannelEnum.EMAIL, "Thank you for your donation", "Dear {{name}}, thank you!",
                "name", "en", true, LocalDateTime.now(), LocalDateTime.now()
        );

        createRequest = new NotificationTemplateCreateRequest(
                "DONATION_THANK_YOU", "Donation Thank You",
                ChannelEnum.EMAIL, "Thank you for your donation",
                "Dear {{name}}, thank you!", "name", "en"
        );
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("should create template successfully")
        void shouldCreateTemplateSuccessfully() {
            when(templateMapper.toEntity(createRequest)).thenReturn(template);
            when(templateRepository.save(any(NotificationTemplate.class))).thenReturn(template);
            when(templateMapper.toResponse(template)).thenReturn(templateResponse);

            NotificationTemplateResponse result = templateService.create(createRequest);

            assertThat(result).isNotNull();
            assertThat(result.templateCode()).isEqualTo("DONATION_THANK_YOU");
            assertThat(template.isActive()).isTrue();
            verify(templateRepository).save(any(NotificationTemplate.class));
        }
    }

    @Nested
    @DisplayName("getById")
    class GetById {

        @Test
        @DisplayName("should return template when found")
        void shouldReturnTemplateWhenFound() {
            when(templateRepository.findById(templateId)).thenReturn(Optional.of(template));
            when(templateMapper.toResponse(template)).thenReturn(templateResponse);

            NotificationTemplateResponse result = templateService.getById(templateId);

            assertThat(result).isNotNull();
            assertThat(result.templateCode()).isEqualTo("DONATION_THANK_YOU");
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when not found")
        void shouldThrowResourceNotFoundWhenNotFound() {
            when(templateRepository.findById(templateId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> templateService.getById(templateId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getAll")
    class GetAll {

        @Test
        @DisplayName("should return all templates")
        void shouldReturnAllTemplates() {
            List<NotificationTemplate> templates = List.of(template);
            List<NotificationTemplateResponse> responses = List.of(templateResponse);
            when(templateRepository.findAll()).thenReturn(templates);
            when(templateMapper.toResponseList(templates)).thenReturn(responses);

            List<NotificationTemplateResponse> result = templateService.getAll();

            assertThat(result).hasSize(1);
            verify(templateRepository).findAll();
        }
    }

    @Nested
    @DisplayName("getActiveTemplates")
    class GetActiveTemplates {

        @Test
        @DisplayName("should return active templates")
        void shouldReturnActiveTemplates() {
            List<NotificationTemplate> templates = List.of(template);
            List<NotificationTemplateResponse> responses = List.of(templateResponse);
            when(templateRepository.findByIsActiveTrue()).thenReturn(templates);
            when(templateMapper.toResponseList(templates)).thenReturn(responses);

            List<NotificationTemplateResponse> result = templateService.getActiveTemplates();

            assertThat(result).hasSize(1);
            verify(templateRepository).findByIsActiveTrue();
        }
    }

    @Nested
    @DisplayName("getByLanguage")
    class GetByLanguage {

        @Test
        @DisplayName("should return templates by language")
        void shouldReturnTemplatesByLanguage() {
            List<NotificationTemplate> templates = List.of(template);
            List<NotificationTemplateResponse> responses = List.of(templateResponse);
            when(templateRepository.findByLanguage("en")).thenReturn(templates);
            when(templateMapper.toResponseList(templates)).thenReturn(responses);

            List<NotificationTemplateResponse> result = templateService.getByLanguage("en");

            assertThat(result).hasSize(1);
            verify(templateRepository).findByLanguage("en");
        }
    }

    @Nested
    @DisplayName("deactivate")
    class Deactivate {

        @Test
        @DisplayName("should deactivate template")
        void shouldDeactivateTemplate() {
            when(templateRepository.findById(templateId)).thenReturn(Optional.of(template));
            when(templateRepository.save(any(NotificationTemplate.class))).thenReturn(template);
            when(templateMapper.toResponse(template)).thenReturn(templateResponse);

            NotificationTemplateResponse result = templateService.deactivate(templateId);

            assertThat(result).isNotNull();
            assertThat(template.isActive()).isFalse();
            verify(templateRepository).save(template);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when not found")
        void shouldThrowResourceNotFoundWhenNotFound() {
            when(templateRepository.findById(templateId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> templateService.deactivate(templateId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
