package com.bloodbank.complianceservice.service;

import com.bloodbank.common.exceptions.ResourceNotFoundException;
import com.bloodbank.complianceservice.dto.RegulatoryFrameworkCreateRequest;
import com.bloodbank.complianceservice.dto.RegulatoryFrameworkResponse;
import com.bloodbank.complianceservice.entity.RegulatoryFramework;
import com.bloodbank.complianceservice.mapper.RegulatoryFrameworkMapper;
import com.bloodbank.complianceservice.repository.RegulatoryFrameworkRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
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
class ComplianceServiceTest {

    @Mock
    private RegulatoryFrameworkRepository frameworkRepository;

    @Mock
    private RegulatoryFrameworkMapper frameworkMapper;

    @InjectMocks
    private ComplianceService complianceService;

    private UUID frameworkId;
    private UUID countryId;
    private RegulatoryFramework framework;
    private RegulatoryFrameworkResponse frameworkResponse;
    private RegulatoryFrameworkCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        frameworkId = UUID.randomUUID();
        countryId = UUID.randomUUID();

        framework = new RegulatoryFramework();
        framework.setId(frameworkId);
        framework.setFrameworkCode("AABB-001");
        framework.setFrameworkName("AABB Standards");
        framework.setAuthorityName("AABB");
        framework.setCountryId(countryId);
        framework.setDescription("Blood bank standards");
        framework.setEffectiveDate(LocalDate.of(2024, 1, 1));
        framework.setVersionNumber("1.0");
        framework.setDocumentUrl("https://example.com/aabb");
        framework.setActive(true);

        frameworkResponse = new RegulatoryFrameworkResponse(
                frameworkId, "AABB-001", "AABB Standards", "AABB",
                countryId, "Blood bank standards",
                LocalDate.of(2024, 1, 1), "1.0",
                "https://example.com/aabb", true,
                LocalDateTime.now(), LocalDateTime.now()
        );

        createRequest = new RegulatoryFrameworkCreateRequest(
                "AABB-001", "AABB Standards", "AABB",
                countryId, "Blood bank standards",
                LocalDate.of(2024, 1, 1), "1.0",
                "https://example.com/aabb"
        );
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("should create regulatory framework successfully")
        void shouldCreateFrameworkSuccessfully() {
            when(frameworkMapper.toEntity(createRequest)).thenReturn(framework);
            when(frameworkRepository.save(any(RegulatoryFramework.class))).thenReturn(framework);
            when(frameworkMapper.toResponse(framework)).thenReturn(frameworkResponse);

            RegulatoryFrameworkResponse result = complianceService.create(createRequest);

            assertThat(result).isNotNull();
            assertThat(result.frameworkCode()).isEqualTo("AABB-001");
            assertThat(result.frameworkName()).isEqualTo("AABB Standards");
            verify(frameworkRepository).save(any(RegulatoryFramework.class));
        }
    }

    @Nested
    @DisplayName("getById")
    class GetById {

        @Test
        @DisplayName("should return framework when found")
        void shouldReturnFrameworkWhenFound() {
            when(frameworkRepository.findById(frameworkId)).thenReturn(Optional.of(framework));
            when(frameworkMapper.toResponse(framework)).thenReturn(frameworkResponse);

            RegulatoryFrameworkResponse result = complianceService.getById(frameworkId);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(frameworkId);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            when(frameworkRepository.findById(frameworkId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> complianceService.getById(frameworkId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getAll")
    class GetAll {

        @Test
        @DisplayName("should return all frameworks")
        void shouldReturnAllFrameworks() {
            List<RegulatoryFramework> frameworks = List.of(framework);
            List<RegulatoryFrameworkResponse> responses = List.of(frameworkResponse);
            when(frameworkRepository.findAll()).thenReturn(frameworks);
            when(frameworkMapper.toResponseList(frameworks)).thenReturn(responses);

            List<RegulatoryFrameworkResponse> result = complianceService.getAll();

            assertThat(result).hasSize(1);
            verify(frameworkRepository).findAll();
        }
    }

    @Nested
    @DisplayName("getByFrameworkCode")
    class GetByFrameworkCode {

        @Test
        @DisplayName("should return framework by code")
        void shouldReturnByCode() {
            when(frameworkRepository.findByFrameworkCode("AABB-001")).thenReturn(Optional.of(framework));
            when(frameworkMapper.toResponse(framework)).thenReturn(frameworkResponse);

            RegulatoryFrameworkResponse result = complianceService.getByFrameworkCode("AABB-001");

            assertThat(result).isNotNull();
            assertThat(result.frameworkCode()).isEqualTo("AABB-001");
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when code not found")
        void shouldThrowWhenCodeNotFound() {
            when(frameworkRepository.findByFrameworkCode("UNKNOWN")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> complianceService.getByFrameworkCode("UNKNOWN"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getActiveFrameworks")
    class GetActiveFrameworks {

        @Test
        @DisplayName("should return active frameworks")
        void shouldReturnActiveFrameworks() {
            List<RegulatoryFramework> activeFrameworks = List.of(framework);
            List<RegulatoryFrameworkResponse> responses = List.of(frameworkResponse);
            when(frameworkRepository.findByIsActive(true)).thenReturn(activeFrameworks);
            when(frameworkMapper.toResponseList(activeFrameworks)).thenReturn(responses);

            List<RegulatoryFrameworkResponse> result = complianceService.getActiveFrameworks();

            assertThat(result).hasSize(1);
            verify(frameworkRepository).findByIsActive(true);
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("should update framework successfully")
        void shouldUpdateFrameworkSuccessfully() {
            when(frameworkRepository.findById(frameworkId)).thenReturn(Optional.of(framework));
            when(frameworkRepository.save(any(RegulatoryFramework.class))).thenReturn(framework);
            when(frameworkMapper.toResponse(framework)).thenReturn(frameworkResponse);

            RegulatoryFrameworkResponse result = complianceService.update(frameworkId, createRequest);

            assertThat(result).isNotNull();
            verify(frameworkRepository).save(any(RegulatoryFramework.class));
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when framework not found")
        void shouldThrowWhenNotFound() {
            when(frameworkRepository.findById(frameworkId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> complianceService.update(frameworkId, createRequest))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("deactivate")
    class Deactivate {

        @Test
        @DisplayName("should deactivate framework successfully")
        void shouldDeactivateSuccessfully() {
            when(frameworkRepository.findById(frameworkId)).thenReturn(Optional.of(framework));
            when(frameworkRepository.save(any(RegulatoryFramework.class))).thenReturn(framework);
            when(frameworkMapper.toResponse(framework)).thenReturn(frameworkResponse);

            RegulatoryFrameworkResponse result = complianceService.deactivate(frameworkId);

            assertThat(result).isNotNull();
            assertThat(framework.isActive()).isFalse();
            verify(frameworkRepository).save(framework);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when framework not found")
        void shouldThrowWhenNotFound() {
            when(frameworkRepository.findById(frameworkId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> complianceService.deactivate(frameworkId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
