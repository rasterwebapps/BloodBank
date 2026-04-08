package com.bloodbank.requestmatchingservice.service;

import com.bloodbank.common.dto.PagedResponse;
import com.bloodbank.common.exceptions.BusinessException;
import com.bloodbank.common.exceptions.ResourceNotFoundException;
import com.bloodbank.requestmatchingservice.dto.DisasterEventCreateRequest;
import com.bloodbank.requestmatchingservice.dto.DisasterEventResponse;
import com.bloodbank.requestmatchingservice.dto.DonorMobilizationCreateRequest;
import com.bloodbank.requestmatchingservice.dto.DonorMobilizationResponse;
import com.bloodbank.requestmatchingservice.entity.DisasterEvent;
import com.bloodbank.requestmatchingservice.entity.DonorMobilization;
import com.bloodbank.requestmatchingservice.enums.DisasterSeverityEnum;
import com.bloodbank.requestmatchingservice.enums.DisasterStatusEnum;
import com.bloodbank.requestmatchingservice.enums.DisasterTypeEnum;
import com.bloodbank.requestmatchingservice.enums.MobilizationStatusEnum;
import com.bloodbank.requestmatchingservice.enums.MobilizationTypeEnum;
import com.bloodbank.requestmatchingservice.mapper.DisasterEventMapper;
import com.bloodbank.requestmatchingservice.mapper.DonorMobilizationMapper;
import com.bloodbank.requestmatchingservice.repository.DisasterEventRepository;
import com.bloodbank.requestmatchingservice.repository.DonorMobilizationRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DisasterResponseServiceTest {

    @Mock private DisasterEventRepository disasterEventRepository;
    @Mock private DonorMobilizationRepository donorMobilizationRepository;
    @Mock private DisasterEventMapper disasterEventMapper;
    @Mock private DonorMobilizationMapper donorMobilizationMapper;
    @InjectMocks private DisasterResponseService disasterResponseService;

    private UUID disasterId;
    private UUID mobilizationId;
    private UUID branchId;
    private UUID donorId;
    private DisasterEvent disasterEvent;
    private DisasterEventResponse disasterEventResponse;
    private DonorMobilization donorMobilization;
    private DonorMobilizationResponse donorMobilizationResponse;

    @BeforeEach
    void setUp() {
        disasterId = UUID.randomUUID();
        mobilizationId = UUID.randomUUID();
        branchId = UUID.randomUUID();
        donorId = UUID.randomUUID();

        disasterEvent = new DisasterEvent();
        disasterEvent.setEventCode("DE-ABCD1234");
        disasterEvent.setEventName("Earthquake Relief");
        disasterEvent.setEventType(DisasterTypeEnum.NATURAL_DISASTER);
        disasterEvent.setSeverity(DisasterSeverityEnum.HIGH);
        disasterEvent.setStatus(DisasterStatusEnum.ACTIVE);
        disasterEvent.setStartDate(Instant.now());
        disasterEvent.setEstimatedCasualties(200);
        disasterEvent.setBloodUnitsNeeded(500);
        disasterEvent.setCoordinatorName("Coordinator A");
        disasterEvent.setBranchId(branchId);

        disasterEventResponse = new DisasterEventResponse(
                disasterId, "DE-ABCD1234", "Earthquake Relief",
                DisasterTypeEnum.NATURAL_DISASTER, DisasterSeverityEnum.HIGH,
                null, null, Instant.now(), null, 200, 500,
                "Coordinator A", null, DisasterStatusEnum.ACTIVE,
                null, branchId, LocalDateTime.now(), LocalDateTime.now());

        donorMobilization = new DonorMobilization();
        donorMobilization.setDisasterEventId(disasterId);
        donorMobilization.setDonorId(donorId);
        donorMobilization.setContactMethod(MobilizationTypeEnum.SMS);
        donorMobilization.setContactedAt(Instant.now());
        donorMobilization.setDonationCompleted(false);
        donorMobilization.setBranchId(branchId);

        donorMobilizationResponse = new DonorMobilizationResponse(
                mobilizationId, disasterId, null, donorId,
                MobilizationTypeEnum.SMS, Instant.now(), null, null,
                null, false, null, null, branchId,
                LocalDateTime.now(), LocalDateTime.now());
    }

    @Nested
    @DisplayName("createDisasterEvent")
    class CreateDisasterEvent {

        @Test
        @DisplayName("Should create disaster event successfully")
        void createDisasterEvent_success() {
            DisasterEventCreateRequest createRequest = new DisasterEventCreateRequest(
                    "Earthquake Relief", DisasterTypeEnum.NATURAL_DISASTER,
                    DisasterSeverityEnum.HIGH, "City Center", null,
                    Instant.now(), 200, 500, "Coordinator A", "+123456789",
                    null, branchId);

            when(disasterEventMapper.toEntity(createRequest)).thenReturn(disasterEvent);
            when(disasterEventRepository.save(any(DisasterEvent.class))).thenReturn(disasterEvent);
            when(disasterEventMapper.toResponse(disasterEvent)).thenReturn(disasterEventResponse);

            DisasterEventResponse result = disasterResponseService.createDisasterEvent(createRequest);

            assertThat(result).isNotNull();
            assertThat(result.eventName()).isEqualTo("Earthquake Relief");
            assertThat(result.status()).isEqualTo(DisasterStatusEnum.ACTIVE);
            verify(disasterEventRepository).save(any(DisasterEvent.class));
        }

        @Test
        @DisplayName("Should set ACTIVE status on creation")
        void createDisasterEvent_setsActiveStatus() {
            DisasterEventCreateRequest createRequest = new DisasterEventCreateRequest(
                    "Mass Casualty Event", DisasterTypeEnum.MASS_CASUALTY,
                    DisasterSeverityEnum.CRITICAL, "Highway Intersection", null,
                    null, 50, 100, "Coord B", "+987654321", null, branchId);

            DisasterEvent newEntity = new DisasterEvent();
            when(disasterEventMapper.toEntity(createRequest)).thenReturn(newEntity);
            when(disasterEventRepository.save(any(DisasterEvent.class))).thenReturn(newEntity);

            DisasterEventResponse resp = new DisasterEventResponse(
                    disasterId, "DE-TEST1234", "Mass Casualty Event",
                    DisasterTypeEnum.MASS_CASUALTY, DisasterSeverityEnum.CRITICAL,
                    "Highway Intersection", null, Instant.now(), null, 50, 100,
                    "Coord B", "+987654321", DisasterStatusEnum.ACTIVE,
                    null, branchId, LocalDateTime.now(), LocalDateTime.now());
            when(disasterEventMapper.toResponse(newEntity)).thenReturn(resp);

            DisasterEventResponse result = disasterResponseService.createDisasterEvent(createRequest);

            assertThat(result.status()).isEqualTo(DisasterStatusEnum.ACTIVE);
        }

        @Test
        @DisplayName("Should set startDate to now when not provided")
        void createDisasterEvent_setsStartDateWhenNull() {
            DisasterEventCreateRequest createRequest = new DisasterEventCreateRequest(
                    "Epidemic", DisasterTypeEnum.EPIDEMIC,
                    DisasterSeverityEnum.MEDIUM, "Region X", null,
                    null, 10, 30, "Coord C", null, null, branchId);

            DisasterEvent entityWithNullStart = new DisasterEvent();
            entityWithNullStart.setStartDate(null);
            when(disasterEventMapper.toEntity(createRequest)).thenReturn(entityWithNullStart);
            when(disasterEventRepository.save(any(DisasterEvent.class))).thenReturn(entityWithNullStart);
            when(disasterEventMapper.toResponse(entityWithNullStart)).thenReturn(disasterEventResponse);

            disasterResponseService.createDisasterEvent(createRequest);

            assertThat(entityWithNullStart.getStartDate()).isNotNull();
            verify(disasterEventRepository).save(entityWithNullStart);
        }
    }

    @Nested
    @DisplayName("getDisasterEventById")
    class GetDisasterEventById {

        @Test
        @DisplayName("Should return disaster event when found")
        void getById_success() {
            when(disasterEventRepository.findById(disasterId)).thenReturn(Optional.of(disasterEvent));
            when(disasterEventMapper.toResponse(disasterEvent)).thenReturn(disasterEventResponse);

            DisasterEventResponse result = disasterResponseService.getDisasterEventById(disasterId);

            assertThat(result.eventName()).isEqualTo("Earthquake Relief");
        }

        @Test
        @DisplayName("Should throw when disaster event not found")
        void getById_notFound() {
            when(disasterEventRepository.findById(disasterId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> disasterResponseService.getDisasterEventById(disasterId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getByEventCode")
    class GetByEventCode {

        @Test
        @DisplayName("Should return disaster event by code")
        void getByEventCode_success() {
            when(disasterEventRepository.findByEventCode("DE-ABCD1234"))
                    .thenReturn(Optional.of(disasterEvent));
            when(disasterEventMapper.toResponse(disasterEvent)).thenReturn(disasterEventResponse);

            DisasterEventResponse result = disasterResponseService.getByEventCode("DE-ABCD1234");

            assertThat(result.eventCode()).isEqualTo("DE-ABCD1234");
        }

        @Test
        @DisplayName("Should throw when event code not found")
        void getByEventCode_notFound() {
            when(disasterEventRepository.findByEventCode("DE-NOTEXIST"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> disasterResponseService.getByEventCode("DE-NOTEXIST"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getByStatus")
    class GetByStatus {

        @Test
        @DisplayName("Should return paged disaster events by status")
        void getByStatus_success() {
            Pageable pageable = PageRequest.of(0, 20);
            Page<DisasterEvent> page = new PageImpl<>(List.of(disasterEvent), pageable, 1);
            when(disasterEventRepository.findByStatus(DisasterStatusEnum.ACTIVE, pageable))
                    .thenReturn(page);
            when(disasterEventMapper.toResponseList(any()))
                    .thenReturn(List.of(disasterEventResponse));

            PagedResponse<DisasterEventResponse> result = disasterResponseService.getByStatus(
                    DisasterStatusEnum.ACTIVE, pageable);

            assertThat(result.totalElements()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("getActiveEvents")
    class GetActiveEvents {

        @Test
        @DisplayName("Should return active and escalated events")
        void getActiveEvents_success() {
            when(disasterEventRepository.findByStatusIn(
                    List.of(DisasterStatusEnum.ACTIVE, DisasterStatusEnum.ESCALATED)))
                    .thenReturn(List.of(disasterEvent));
            when(disasterEventMapper.toResponseList(List.of(disasterEvent)))
                    .thenReturn(List.of(disasterEventResponse));

            List<DisasterEventResponse> result = disasterResponseService.getActiveEvents();

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should return empty list when no active events")
        void getActiveEvents_empty() {
            when(disasterEventRepository.findByStatusIn(
                    List.of(DisasterStatusEnum.ACTIVE, DisasterStatusEnum.ESCALATED)))
                    .thenReturn(List.of());
            when(disasterEventMapper.toResponseList(List.of())).thenReturn(List.of());

            List<DisasterEventResponse> result = disasterResponseService.getActiveEvents();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("escalateEvent")
    class EscalateEvent {

        @Test
        @DisplayName("Should escalate from LOW to MEDIUM")
        void escalate_lowToMedium() {
            disasterEvent.setSeverity(DisasterSeverityEnum.LOW);
            when(disasterEventRepository.findById(disasterId)).thenReturn(Optional.of(disasterEvent));
            when(disasterEventRepository.save(any(DisasterEvent.class))).thenReturn(disasterEvent);

            DisasterEventResponse escalatedResponse = new DisasterEventResponse(
                    disasterId, "DE-ABCD1234", "Earthquake Relief",
                    DisasterTypeEnum.NATURAL_DISASTER, DisasterSeverityEnum.MEDIUM,
                    null, null, Instant.now(), null, 200, 500,
                    "Coordinator A", null, DisasterStatusEnum.ESCALATED,
                    null, branchId, LocalDateTime.now(), LocalDateTime.now());
            when(disasterEventMapper.toResponse(disasterEvent)).thenReturn(escalatedResponse);

            DisasterEventResponse result = disasterResponseService.escalateEvent(disasterId);

            assertThat(result.severity()).isEqualTo(DisasterSeverityEnum.MEDIUM);
            assertThat(result.status()).isEqualTo(DisasterStatusEnum.ESCALATED);
            verify(disasterEventRepository).save(any(DisasterEvent.class));
        }

        @Test
        @DisplayName("Should escalate from MEDIUM to HIGH")
        void escalate_mediumToHigh() {
            disasterEvent.setSeverity(DisasterSeverityEnum.MEDIUM);
            when(disasterEventRepository.findById(disasterId)).thenReturn(Optional.of(disasterEvent));
            when(disasterEventRepository.save(any(DisasterEvent.class))).thenReturn(disasterEvent);

            DisasterEventResponse escalatedResponse = new DisasterEventResponse(
                    disasterId, "DE-ABCD1234", "Earthquake Relief",
                    DisasterTypeEnum.NATURAL_DISASTER, DisasterSeverityEnum.HIGH,
                    null, null, Instant.now(), null, 200, 500,
                    "Coordinator A", null, DisasterStatusEnum.ESCALATED,
                    null, branchId, LocalDateTime.now(), LocalDateTime.now());
            when(disasterEventMapper.toResponse(disasterEvent)).thenReturn(escalatedResponse);

            DisasterEventResponse result = disasterResponseService.escalateEvent(disasterId);

            assertThat(result.severity()).isEqualTo(DisasterSeverityEnum.HIGH);
        }

        @Test
        @DisplayName("Should escalate from HIGH to CRITICAL")
        void escalate_highToCritical() {
            disasterEvent.setSeverity(DisasterSeverityEnum.HIGH);
            when(disasterEventRepository.findById(disasterId)).thenReturn(Optional.of(disasterEvent));
            when(disasterEventRepository.save(any(DisasterEvent.class))).thenReturn(disasterEvent);

            DisasterEventResponse escalatedResponse = new DisasterEventResponse(
                    disasterId, "DE-ABCD1234", "Earthquake Relief",
                    DisasterTypeEnum.NATURAL_DISASTER, DisasterSeverityEnum.CRITICAL,
                    null, null, Instant.now(), null, 200, 500,
                    "Coordinator A", null, DisasterStatusEnum.ESCALATED,
                    null, branchId, LocalDateTime.now(), LocalDateTime.now());
            when(disasterEventMapper.toResponse(disasterEvent)).thenReturn(escalatedResponse);

            DisasterEventResponse result = disasterResponseService.escalateEvent(disasterId);

            assertThat(result.severity()).isEqualTo(DisasterSeverityEnum.CRITICAL);
        }

        @Test
        @DisplayName("Should not change severity when already CRITICAL")
        void escalate_alreadyCritical_noSeverityChange() {
            disasterEvent.setSeverity(DisasterSeverityEnum.CRITICAL);
            when(disasterEventRepository.findById(disasterId)).thenReturn(Optional.of(disasterEvent));
            when(disasterEventRepository.save(any(DisasterEvent.class))).thenReturn(disasterEvent);
            when(disasterEventMapper.toResponse(disasterEvent)).thenReturn(disasterEventResponse);

            DisasterEventResponse result = disasterResponseService.escalateEvent(disasterId);

            assertThat(result).isNotNull();
            verify(disasterEventRepository).save(any(DisasterEvent.class));
        }

        @Test
        @DisplayName("Should throw BusinessException when escalating closed event")
        void escalate_closedEvent_throwsException() {
            disasterEvent.setStatus(DisasterStatusEnum.CLOSED);
            when(disasterEventRepository.findById(disasterId)).thenReturn(Optional.of(disasterEvent));

            assertThatThrownBy(() -> disasterResponseService.escalateEvent(disasterId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Cannot escalate a closed disaster event");
        }

        @Test
        @DisplayName("Should throw when disaster event not found")
        void escalate_notFound() {
            when(disasterEventRepository.findById(disasterId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> disasterResponseService.escalateEvent(disasterId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("closeEvent")
    class CloseEvent {

        @Test
        @DisplayName("Should close disaster event and set end date")
        void closeEvent_success() {
            when(disasterEventRepository.findById(disasterId)).thenReturn(Optional.of(disasterEvent));
            when(disasterEventRepository.save(any(DisasterEvent.class))).thenReturn(disasterEvent);

            DisasterEventResponse closedResponse = new DisasterEventResponse(
                    disasterId, "DE-ABCD1234", "Earthquake Relief",
                    DisasterTypeEnum.NATURAL_DISASTER, DisasterSeverityEnum.HIGH,
                    null, null, Instant.now(), Instant.now(), 200, 500,
                    "Coordinator A", null, DisasterStatusEnum.CLOSED,
                    null, branchId, LocalDateTime.now(), LocalDateTime.now());
            when(disasterEventMapper.toResponse(disasterEvent)).thenReturn(closedResponse);

            DisasterEventResponse result = disasterResponseService.closeEvent(disasterId);

            assertThat(result.status()).isEqualTo(DisasterStatusEnum.CLOSED);
            assertThat(result.endDate()).isNotNull();
            verify(disasterEventRepository).save(any(DisasterEvent.class));
        }

        @Test
        @DisplayName("Should throw when closing non-existent event")
        void closeEvent_notFound() {
            when(disasterEventRepository.findById(disasterId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> disasterResponseService.closeEvent(disasterId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("mobilizeDonor")
    class MobilizeDonor {

        @Test
        @DisplayName("Should mobilize donor for disaster event successfully")
        void mobilizeDonor_success() {
            DonorMobilizationCreateRequest createRequest = new DonorMobilizationCreateRequest(
                    disasterId, null, donorId, MobilizationTypeEnum.SMS,
                    Instant.now().plusSeconds(7200), null, branchId);

            when(disasterEventRepository.findById(disasterId)).thenReturn(Optional.of(disasterEvent));
            when(donorMobilizationRepository.existsByDisasterEventIdAndDonorId(disasterId, donorId))
                    .thenReturn(false);
            when(donorMobilizationMapper.toEntity(createRequest)).thenReturn(donorMobilization);
            when(donorMobilizationRepository.save(any(DonorMobilization.class))).thenReturn(donorMobilization);
            when(donorMobilizationMapper.toResponse(donorMobilization)).thenReturn(donorMobilizationResponse);

            DonorMobilizationResponse result = disasterResponseService.mobilizeDonor(createRequest);

            assertThat(result).isNotNull();
            assertThat(result.donorId()).isEqualTo(donorId);
            assertThat(result.donationCompleted()).isFalse();
            verify(donorMobilizationRepository).save(any(DonorMobilization.class));
        }

        @Test
        @DisplayName("Should throw when donor already mobilized for same disaster")
        void mobilizeDonor_alreadyMobilized_throwsException() {
            DonorMobilizationCreateRequest createRequest = new DonorMobilizationCreateRequest(
                    disasterId, null, donorId, MobilizationTypeEnum.SMS,
                    null, null, branchId);

            when(disasterEventRepository.findById(disasterId)).thenReturn(Optional.of(disasterEvent));
            when(donorMobilizationRepository.existsByDisasterEventIdAndDonorId(disasterId, donorId))
                    .thenReturn(true);

            assertThatThrownBy(() -> disasterResponseService.mobilizeDonor(createRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Donor already mobilized");
        }

        @Test
        @DisplayName("Should throw when disaster event not found for mobilization")
        void mobilizeDonor_disasterNotFound() {
            DonorMobilizationCreateRequest createRequest = new DonorMobilizationCreateRequest(
                    disasterId, null, donorId, MobilizationTypeEnum.PHONE,
                    null, null, branchId);

            when(disasterEventRepository.findById(disasterId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> disasterResponseService.mobilizeDonor(createRequest))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should allow mobilization without disaster event (emergency request only)")
        void mobilizeDonor_withoutDisasterEvent() {
            DonorMobilizationCreateRequest createRequest = new DonorMobilizationCreateRequest(
                    null, UUID.randomUUID(), donorId, MobilizationTypeEnum.EMAIL,
                    null, null, branchId);

            when(donorMobilizationMapper.toEntity(createRequest)).thenReturn(donorMobilization);
            when(donorMobilizationRepository.save(any(DonorMobilization.class))).thenReturn(donorMobilization);
            when(donorMobilizationMapper.toResponse(donorMobilization)).thenReturn(donorMobilizationResponse);

            DonorMobilizationResponse result = disasterResponseService.mobilizeDonor(createRequest);

            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("getMobilizationById")
    class GetMobilizationById {

        @Test
        @DisplayName("Should return mobilization when found")
        void getMobilizationById_success() {
            when(donorMobilizationRepository.findById(mobilizationId))
                    .thenReturn(Optional.of(donorMobilization));
            when(donorMobilizationMapper.toResponse(donorMobilization))
                    .thenReturn(donorMobilizationResponse);

            DonorMobilizationResponse result = disasterResponseService.getMobilizationById(mobilizationId);

            assertThat(result).isNotNull();
            assertThat(result.donorId()).isEqualTo(donorId);
        }

        @Test
        @DisplayName("Should throw when mobilization not found")
        void getMobilizationById_notFound() {
            when(donorMobilizationRepository.findById(mobilizationId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> disasterResponseService.getMobilizationById(mobilizationId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getMobilizationsByDisaster")
    class GetMobilizationsByDisaster {

        @Test
        @DisplayName("Should return mobilizations for a disaster event")
        void getMobilizationsByDisaster_success() {
            when(donorMobilizationRepository.findByDisasterEventId(disasterId))
                    .thenReturn(List.of(donorMobilization));
            when(donorMobilizationMapper.toResponseList(List.of(donorMobilization)))
                    .thenReturn(List.of(donorMobilizationResponse));

            List<DonorMobilizationResponse> result = disasterResponseService
                    .getMobilizationsByDisaster(disasterId);

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getMobilizationsByDisasterPaged")
    class GetMobilizationsByDisasterPaged {

        @Test
        @DisplayName("Should return paged mobilizations for disaster")
        void getMobilizationsByDisasterPaged_success() {
            Pageable pageable = PageRequest.of(0, 20);
            Page<DonorMobilization> page = new PageImpl<>(List.of(donorMobilization), pageable, 1);
            when(donorMobilizationRepository.findByDisasterEventId(disasterId, pageable))
                    .thenReturn(page);
            when(donorMobilizationMapper.toResponseList(any()))
                    .thenReturn(List.of(donorMobilizationResponse));

            PagedResponse<DonorMobilizationResponse> result = disasterResponseService
                    .getMobilizationsByDisasterPaged(disasterId, pageable);

            assertThat(result.totalElements()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("recordResponse")
    class RecordResponse {

        @Test
        @DisplayName("Should record donor response to mobilization as ACCEPTED")
        void recordResponse_accepted() {
            when(donorMobilizationRepository.findById(mobilizationId))
                    .thenReturn(Optional.of(donorMobilization));
            when(donorMobilizationRepository.save(any(DonorMobilization.class)))
                    .thenReturn(donorMobilization);

            DonorMobilizationResponse acceptedResponse = new DonorMobilizationResponse(
                    mobilizationId, disasterId, null, donorId,
                    MobilizationTypeEnum.SMS, Instant.now(), MobilizationStatusEnum.ACCEPTED,
                    Instant.now(), null, false, null, null, branchId,
                    LocalDateTime.now(), LocalDateTime.now());
            when(donorMobilizationMapper.toResponse(donorMobilization)).thenReturn(acceptedResponse);

            DonorMobilizationResponse result = disasterResponseService.recordResponse(
                    mobilizationId, MobilizationStatusEnum.ACCEPTED);

            assertThat(result.response()).isEqualTo(MobilizationStatusEnum.ACCEPTED);
            verify(donorMobilizationRepository).save(any(DonorMobilization.class));
        }

        @Test
        @DisplayName("Should record donor response as DECLINED")
        void recordResponse_declined() {
            when(donorMobilizationRepository.findById(mobilizationId))
                    .thenReturn(Optional.of(donorMobilization));
            when(donorMobilizationRepository.save(any(DonorMobilization.class)))
                    .thenReturn(donorMobilization);

            DonorMobilizationResponse declinedResponse = new DonorMobilizationResponse(
                    mobilizationId, disasterId, null, donorId,
                    MobilizationTypeEnum.SMS, Instant.now(), MobilizationStatusEnum.DECLINED,
                    Instant.now(), null, false, null, null, branchId,
                    LocalDateTime.now(), LocalDateTime.now());
            when(donorMobilizationMapper.toResponse(donorMobilization)).thenReturn(declinedResponse);

            DonorMobilizationResponse result = disasterResponseService.recordResponse(
                    mobilizationId, MobilizationStatusEnum.DECLINED);

            assertThat(result.response()).isEqualTo(MobilizationStatusEnum.DECLINED);
        }

        @Test
        @DisplayName("Should throw when mobilization not found for response")
        void recordResponse_notFound() {
            when(donorMobilizationRepository.findById(mobilizationId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> disasterResponseService.recordResponse(
                    mobilizationId, MobilizationStatusEnum.ACCEPTED))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("markDonationCompleted")
    class MarkDonationCompleted {

        @Test
        @DisplayName("Should mark donation as completed with collection ID")
        void markDonationCompleted_success() {
            UUID collectionId = UUID.randomUUID();
            when(donorMobilizationRepository.findById(mobilizationId))
                    .thenReturn(Optional.of(donorMobilization));
            when(donorMobilizationRepository.save(any(DonorMobilization.class)))
                    .thenReturn(donorMobilization);

            DonorMobilizationResponse completedResponse = new DonorMobilizationResponse(
                    mobilizationId, disasterId, null, donorId,
                    MobilizationTypeEnum.SMS, Instant.now(), MobilizationStatusEnum.ACCEPTED,
                    Instant.now(), null, true, collectionId, null, branchId,
                    LocalDateTime.now(), LocalDateTime.now());
            when(donorMobilizationMapper.toResponse(donorMobilization)).thenReturn(completedResponse);

            DonorMobilizationResponse result = disasterResponseService.markDonationCompleted(
                    mobilizationId, collectionId);

            assertThat(result.donationCompleted()).isTrue();
            assertThat(result.collectionId()).isEqualTo(collectionId);
            verify(donorMobilizationRepository).save(any(DonorMobilization.class));
        }

        @Test
        @DisplayName("Should throw when mobilization not found for completion")
        void markDonationCompleted_notFound() {
            when(donorMobilizationRepository.findById(mobilizationId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> disasterResponseService.markDonationCompleted(
                    mobilizationId, UUID.randomUUID()))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
