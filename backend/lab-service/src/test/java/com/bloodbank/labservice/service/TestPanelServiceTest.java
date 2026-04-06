package com.bloodbank.labservice.service;

import com.bloodbank.common.exceptions.ResourceNotFoundException;
import com.bloodbank.labservice.dto.TestPanelCreateRequest;
import com.bloodbank.labservice.dto.TestPanelResponse;
import com.bloodbank.labservice.entity.TestPanel;
import com.bloodbank.labservice.mapper.TestPanelMapper;
import com.bloodbank.labservice.repository.TestPanelRepository;

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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TestPanelServiceTest {

    @Mock
    private TestPanelRepository testPanelRepository;

    @Mock
    private TestPanelMapper testPanelMapper;

    @InjectMocks
    private TestPanelService testPanelService;

    private UUID panelId;
    private TestPanel testPanel;
    private TestPanelResponse panelResponse;
    private TestPanelCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        panelId = UUID.randomUUID();

        testPanel = new TestPanel("TTI", "TTI Panel", "Transmissible infection tests",
                "HIV,HBV,HCV,Syphilis", true);
        testPanel.setId(panelId);
        testPanel.setActive(true);

        panelResponse = new TestPanelResponse(
                panelId, "TTI", "TTI Panel", "Transmissible infection tests",
                "HIV,HBV,HCV,Syphilis", true, true,
                LocalDateTime.now(), LocalDateTime.now()
        );

        createRequest = new TestPanelCreateRequest(
                "TTI", "TTI Panel", "Transmissible infection tests",
                "HIV,HBV,HCV,Syphilis", true
        );
    }

    @Nested
    @DisplayName("createPanel")
    class CreatePanel {

        @Test
        @DisplayName("should create panel successfully")
        void shouldCreatePanelSuccessfully() {
            when(testPanelMapper.toEntity(createRequest)).thenReturn(testPanel);
            when(testPanelRepository.save(any(TestPanel.class))).thenReturn(testPanel);
            when(testPanelMapper.toResponse(testPanel)).thenReturn(panelResponse);

            TestPanelResponse result = testPanelService.createPanel(createRequest);

            assertThat(result).isNotNull();
            assertThat(result.panelCode()).isEqualTo("TTI");
            assertThat(testPanel.isActive()).isTrue();
            verify(testPanelRepository).save(any(TestPanel.class));
        }
    }

    @Nested
    @DisplayName("getActivePanels")
    class GetActivePanels {

        @Test
        @DisplayName("should return active panels")
        void shouldReturnActivePanels() {
            List<TestPanel> panels = List.of(testPanel);
            List<TestPanelResponse> responses = List.of(panelResponse);
            when(testPanelRepository.findByIsActiveTrue()).thenReturn(panels);
            when(testPanelMapper.toResponseList(panels)).thenReturn(responses);

            List<TestPanelResponse> result = testPanelService.getActivePanels();

            assertThat(result).hasSize(1);
            verify(testPanelRepository).findByIsActiveTrue();
        }
    }

    @Nested
    @DisplayName("getMandatoryPanels")
    class GetMandatoryPanels {

        @Test
        @DisplayName("should return mandatory panels")
        void shouldReturnMandatoryPanels() {
            List<TestPanel> panels = List.of(testPanel);
            List<TestPanelResponse> responses = List.of(panelResponse);
            when(testPanelRepository.findByIsMandatoryTrue()).thenReturn(panels);
            when(testPanelMapper.toResponseList(panels)).thenReturn(responses);

            List<TestPanelResponse> result = testPanelService.getMandatoryPanels();

            assertThat(result).hasSize(1);
            verify(testPanelRepository).findByIsMandatoryTrue();
        }
    }

    @Nested
    @DisplayName("getPanelById")
    class GetPanelById {

        @Test
        @DisplayName("should return panel when found")
        void shouldReturnPanelWhenFound() {
            when(testPanelRepository.findById(panelId)).thenReturn(Optional.of(testPanel));
            when(testPanelMapper.toResponse(testPanel)).thenReturn(panelResponse);

            TestPanelResponse result = testPanelService.getPanelById(panelId);

            assertThat(result).isNotNull();
            assertThat(result.panelCode()).isEqualTo("TTI");
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when not found")
        void shouldThrowResourceNotFoundWhenNotFound() {
            when(testPanelRepository.findById(panelId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> testPanelService.getPanelById(panelId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getPanelByCode")
    class GetPanelByCode {

        @Test
        @DisplayName("should return panel by code")
        void shouldReturnPanelByCode() {
            when(testPanelRepository.findByPanelCode("TTI")).thenReturn(Optional.of(testPanel));
            when(testPanelMapper.toResponse(testPanel)).thenReturn(panelResponse);

            TestPanelResponse result = testPanelService.getPanelByCode("TTI");

            assertThat(result).isNotNull();
            assertThat(result.panelCode()).isEqualTo("TTI");
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when code not found")
        void shouldThrowResourceNotFoundWhenCodeNotFound() {
            when(testPanelRepository.findByPanelCode("UNKNOWN")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> testPanelService.getPanelByCode("UNKNOWN"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
