package com.bloodbank.complianceservice.service;

import com.bloodbank.common.exceptions.BusinessException;
import com.bloodbank.common.exceptions.ResourceNotFoundException;
import com.bloodbank.complianceservice.dto.CorrectiveActionRequest;
import com.bloodbank.complianceservice.dto.DeviationCreateRequest;
import com.bloodbank.complianceservice.dto.DeviationResponse;
import com.bloodbank.complianceservice.entity.Deviation;
import com.bloodbank.complianceservice.enums.DeviationCategoryEnum;
import com.bloodbank.complianceservice.enums.DeviationSeverityEnum;
import com.bloodbank.complianceservice.enums.DeviationStatusEnum;
import com.bloodbank.complianceservice.enums.DeviationTypeEnum;
import com.bloodbank.complianceservice.mapper.DeviationMapper;
import com.bloodbank.complianceservice.repository.DeviationRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
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
class DeviationServiceTest {

    @Mock
    private DeviationRepository deviationRepository;

    @Mock
    private DeviationMapper deviationMapper;

    @InjectMocks
    private DeviationService deviationService;

    private UUID deviationId;
    private UUID branchId;
    private Deviation deviation;
    private DeviationResponse deviationResponse;
    private DeviationCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        deviationId = UUID.randomUUID();
        branchId = UUID.randomUUID();

        deviation = new Deviation();
        deviation.setId(deviationId);
        deviation.setDeviationNumber("DEV-12345678");
        deviation.setDeviationType(DeviationTypeEnum.NON_CONFORMANCE);
        deviation.setSeverity(DeviationSeverityEnum.MAJOR);
        deviation.setCategory(DeviationCategoryEnum.COLLECTION);
        deviation.setTitle("Temperature deviation");
        deviation.setDescription("Fridge temperature exceeded limits");
        deviation.setDetectedDate(Instant.now());
        deviation.setStatus(DeviationStatusEnum.OPEN);
        deviation.setBranchId(branchId);

        deviationResponse = new DeviationResponse(
                deviationId, "DEV-12345678", DeviationTypeEnum.NON_CONFORMANCE,
                DeviationSeverityEnum.MAJOR, DeviationCategoryEnum.COLLECTION,
                "Temperature deviation", "Fridge temperature exceeded limits",
                Instant.now(), null, null, null, null, null,
                null, null, DeviationStatusEnum.OPEN, branchId,
                LocalDateTime.now(), LocalDateTime.now()
        );

        createRequest = new DeviationCreateRequest(
                DeviationTypeEnum.NON_CONFORMANCE, DeviationSeverityEnum.MAJOR,
                DeviationCategoryEnum.COLLECTION, "Temperature deviation",
                "Fridge temperature exceeded limits", null, null, branchId
        );
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("should create deviation successfully")
        void shouldCreateDeviationSuccessfully() {
            when(deviationMapper.toEntity(createRequest)).thenReturn(deviation);
            when(deviationRepository.save(any(Deviation.class))).thenReturn(deviation);
            when(deviationMapper.toResponse(deviation)).thenReturn(deviationResponse);

            DeviationResponse result = deviationService.create(createRequest);

            assertThat(result).isNotNull();
            assertThat(result.title()).isEqualTo("Temperature deviation");
            verify(deviationRepository).save(any(Deviation.class));
        }

        @Test
        @DisplayName("should set OPEN status and generate deviation number")
        void shouldSetOpenStatusAndGenerateNumber() {
            Deviation newDeviation = new Deviation();
            when(deviationMapper.toEntity(createRequest)).thenReturn(newDeviation);
            when(deviationRepository.save(any(Deviation.class))).thenReturn(newDeviation);
            when(deviationMapper.toResponse(newDeviation)).thenReturn(deviationResponse);

            deviationService.create(createRequest);

            assertThat(newDeviation.getStatus()).isEqualTo(DeviationStatusEnum.OPEN);
            assertThat(newDeviation.getDeviationNumber()).startsWith("DEV-");
            assertThat(newDeviation.getDetectedDate()).isNotNull();
        }
    }

    @Nested
    @DisplayName("getById")
    class GetById {

        @Test
        @DisplayName("should return deviation when found")
        void shouldReturnDeviationWhenFound() {
            when(deviationRepository.findById(deviationId)).thenReturn(Optional.of(deviation));
            when(deviationMapper.toResponse(deviation)).thenReturn(deviationResponse);

            DeviationResponse result = deviationService.getById(deviationId);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(deviationId);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            when(deviationRepository.findById(deviationId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> deviationService.getById(deviationId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getByDeviationNumber")
    class GetByDeviationNumber {

        @Test
        @DisplayName("should return deviation by number")
        void shouldReturnByNumber() {
            when(deviationRepository.findByDeviationNumber("DEV-12345678")).thenReturn(Optional.of(deviation));
            when(deviationMapper.toResponse(deviation)).thenReturn(deviationResponse);

            DeviationResponse result = deviationService.getByDeviationNumber("DEV-12345678");

            assertThat(result.deviationNumber()).isEqualTo("DEV-12345678");
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when number not found")
        void shouldThrowWhenNumberNotFound() {
            when(deviationRepository.findByDeviationNumber("UNKNOWN")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> deviationService.getByDeviationNumber("UNKNOWN"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getByStatus")
    class GetByStatus {

        @Test
        @DisplayName("should return deviations by status")
        void shouldReturnByStatus() {
            List<Deviation> deviations = List.of(deviation);
            List<DeviationResponse> responses = List.of(deviationResponse);
            when(deviationRepository.findByStatus(DeviationStatusEnum.OPEN)).thenReturn(deviations);
            when(deviationMapper.toResponseList(deviations)).thenReturn(responses);

            List<DeviationResponse> result = deviationService.getByStatus(DeviationStatusEnum.OPEN);

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getBySeverity")
    class GetBySeverity {

        @Test
        @DisplayName("should return deviations by severity")
        void shouldReturnBySeverity() {
            List<Deviation> deviations = List.of(deviation);
            List<DeviationResponse> responses = List.of(deviationResponse);
            when(deviationRepository.findBySeverity(DeviationSeverityEnum.MAJOR)).thenReturn(deviations);
            when(deviationMapper.toResponseList(deviations)).thenReturn(responses);

            List<DeviationResponse> result = deviationService.getBySeverity(DeviationSeverityEnum.MAJOR);

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("investigate")
    class Investigate {

        @Test
        @DisplayName("should start investigation when OPEN")
        void shouldInvestigateWhenOpen() {
            deviation.setStatus(DeviationStatusEnum.OPEN);
            when(deviationRepository.findById(deviationId)).thenReturn(Optional.of(deviation));
            when(deviationRepository.save(any(Deviation.class))).thenReturn(deviation);
            when(deviationMapper.toResponse(deviation)).thenReturn(deviationResponse);

            deviationService.investigate(deviationId);

            assertThat(deviation.getStatus()).isEqualTo(DeviationStatusEnum.UNDER_INVESTIGATION);
            verify(deviationRepository).save(any(Deviation.class));
        }

        @Test
        @DisplayName("should start investigation when REOPENED")
        void shouldInvestigateWhenReopened() {
            deviation.setStatus(DeviationStatusEnum.REOPENED);
            when(deviationRepository.findById(deviationId)).thenReturn(Optional.of(deviation));
            when(deviationRepository.save(any(Deviation.class))).thenReturn(deviation);
            when(deviationMapper.toResponse(deviation)).thenReturn(deviationResponse);

            deviationService.investigate(deviationId);

            assertThat(deviation.getStatus()).isEqualTo(DeviationStatusEnum.UNDER_INVESTIGATION);
        }

        @Test
        @DisplayName("should throw BusinessException when CLOSED")
        void shouldThrowWhenClosed() {
            deviation.setStatus(DeviationStatusEnum.CLOSED);
            when(deviationRepository.findById(deviationId)).thenReturn(Optional.of(deviation));

            assertThatThrownBy(() -> deviationService.investigate(deviationId))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            when(deviationRepository.findById(deviationId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> deviationService.investigate(deviationId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("addCorrectiveAction")
    class AddCorrectiveAction {

        @Test
        @DisplayName("should add corrective action when UNDER_INVESTIGATION")
        void shouldAddCorrectiveAction() {
            deviation.setStatus(DeviationStatusEnum.UNDER_INVESTIGATION);
            CorrectiveActionRequest request = new CorrectiveActionRequest(
                    "Faulty thermostat", "Replace thermostat", "Monthly calibration"
            );

            when(deviationRepository.findById(deviationId)).thenReturn(Optional.of(deviation));
            when(deviationRepository.save(any(Deviation.class))).thenReturn(deviation);
            when(deviationMapper.toResponse(deviation)).thenReturn(deviationResponse);

            deviationService.addCorrectiveAction(deviationId, request);

            assertThat(deviation.getRootCause()).isEqualTo("Faulty thermostat");
            assertThat(deviation.getCorrectiveAction()).isEqualTo("Replace thermostat");
            assertThat(deviation.getPreventiveAction()).isEqualTo("Monthly calibration");
            assertThat(deviation.getStatus()).isEqualTo(DeviationStatusEnum.CORRECTIVE_ACTION);
        }

        @Test
        @DisplayName("should throw BusinessException when not UNDER_INVESTIGATION")
        void shouldThrowWhenNotUnderInvestigation() {
            deviation.setStatus(DeviationStatusEnum.OPEN);
            CorrectiveActionRequest request = new CorrectiveActionRequest(
                    "Root cause", "Fix", "Prevent"
            );

            when(deviationRepository.findById(deviationId)).thenReturn(Optional.of(deviation));

            assertThatThrownBy(() -> deviationService.addCorrectiveAction(deviationId, request))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            CorrectiveActionRequest request = new CorrectiveActionRequest(
                    "Root cause", "Fix", "Prevent"
            );
            when(deviationRepository.findById(deviationId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> deviationService.addCorrectiveAction(deviationId, request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("close")
    class Close {

        @Test
        @DisplayName("should close deviation in CORRECTIVE_ACTION status")
        void shouldCloseDeviation() {
            deviation.setStatus(DeviationStatusEnum.CORRECTIVE_ACTION);
            when(deviationRepository.findById(deviationId)).thenReturn(Optional.of(deviation));
            when(deviationRepository.save(any(Deviation.class))).thenReturn(deviation);
            when(deviationMapper.toResponse(deviation)).thenReturn(deviationResponse);

            deviationService.close(deviationId, "admin");

            assertThat(deviation.getStatus()).isEqualTo(DeviationStatusEnum.CLOSED);
            assertThat(deviation.getClosedBy()).isEqualTo("admin");
            assertThat(deviation.getClosureDate()).isNotNull();
        }

        @Test
        @DisplayName("should throw BusinessException when not in CORRECTIVE_ACTION")
        void shouldThrowWhenNotInCorrectiveAction() {
            deviation.setStatus(DeviationStatusEnum.OPEN);
            when(deviationRepository.findById(deviationId)).thenReturn(Optional.of(deviation));

            assertThatThrownBy(() -> deviationService.close(deviationId, "admin"))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            when(deviationRepository.findById(deviationId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> deviationService.close(deviationId, "admin"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("reopen")
    class Reopen {

        @Test
        @DisplayName("should reopen deviation in CLOSED status")
        void shouldReopenDeviation() {
            deviation.setStatus(DeviationStatusEnum.CLOSED);
            deviation.setClosureDate(Instant.now());
            deviation.setClosedBy("admin");

            when(deviationRepository.findById(deviationId)).thenReturn(Optional.of(deviation));
            when(deviationRepository.save(any(Deviation.class))).thenReturn(deviation);
            when(deviationMapper.toResponse(deviation)).thenReturn(deviationResponse);

            deviationService.reopen(deviationId);

            assertThat(deviation.getStatus()).isEqualTo(DeviationStatusEnum.REOPENED);
            assertThat(deviation.getClosureDate()).isNull();
            assertThat(deviation.getClosedBy()).isNull();
        }

        @Test
        @DisplayName("should throw BusinessException when not CLOSED")
        void shouldThrowWhenNotClosed() {
            deviation.setStatus(DeviationStatusEnum.OPEN);
            when(deviationRepository.findById(deviationId)).thenReturn(Optional.of(deviation));

            assertThatThrownBy(() -> deviationService.reopen(deviationId))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            when(deviationRepository.findById(deviationId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> deviationService.reopen(deviationId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
