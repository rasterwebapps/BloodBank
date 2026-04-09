package com.bloodbank.requestmatchingservice.service;

import com.bloodbank.common.dto.PagedResponse;
import com.bloodbank.common.exceptions.ResourceNotFoundException;
import com.bloodbank.requestmatchingservice.dto.EmergencyRequestCreateRequest;
import com.bloodbank.requestmatchingservice.dto.EmergencyRequestResponse;
import com.bloodbank.requestmatchingservice.entity.EmergencyRequest;
import com.bloodbank.requestmatchingservice.enums.EmergencyPriorityEnum;
import com.bloodbank.requestmatchingservice.enums.EmergencyStatusEnum;
import com.bloodbank.requestmatchingservice.event.RequestMatchingEventPublisher;
import com.bloodbank.requestmatchingservice.mapper.EmergencyRequestMapper;
import com.bloodbank.requestmatchingservice.repository.EmergencyRequestRepository;

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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmergencyServiceTest {

    @Mock private EmergencyRequestRepository emergencyRequestRepository;
    @Mock private EmergencyRequestMapper emergencyRequestMapper;
    @Mock private RequestMatchingEventPublisher eventPublisher;
    @InjectMocks private EmergencyService emergencyService;

    private UUID requestId;
    private UUID branchId;
    private UUID bloodGroupId;
    private UUID componentTypeId;
    private UUID hospitalId;
    private EmergencyRequest emergencyRequest;
    private EmergencyRequestResponse emergencyRequestResponse;

    @BeforeEach
    void setUp() {
        requestId = UUID.randomUUID();
        branchId = UUID.randomUUID();
        bloodGroupId = UUID.randomUUID();
        componentTypeId = UUID.randomUUID();
        hospitalId = UUID.randomUUID();

        emergencyRequest = new EmergencyRequest();
        emergencyRequest.setRequestNumber("ER-ABCD1234");
        emergencyRequest.setHospitalId(hospitalId);
        emergencyRequest.setBloodGroupId(bloodGroupId);
        emergencyRequest.setComponentTypeId(componentTypeId);
        emergencyRequest.setUnitsNeeded(4);
        emergencyRequest.setUnitsFulfilled(0);
        emergencyRequest.setPriority(EmergencyPriorityEnum.EMERGENCY);
        emergencyRequest.setStatus(EmergencyStatusEnum.OPEN);
        emergencyRequest.setBroadcastSent(false);
        emergencyRequest.setBranchId(branchId);
        emergencyRequest.setPatientName("John Doe");
        emergencyRequest.setRequiredBy(Instant.now().plusSeconds(3600));

        emergencyRequestResponse = new EmergencyRequestResponse(
                requestId, "ER-ABCD1234", hospitalId, bloodGroupId, componentTypeId,
                4, 0, EmergencyPriorityEnum.EMERGENCY, "John Doe", null,
                null, Instant.now().plusSeconds(3600), EmergencyStatusEnum.OPEN,
                false, null, null, branchId,
                LocalDateTime.now(), LocalDateTime.now());
    }

    @Nested
    @DisplayName("createEmergencyRequest")
    class CreateEmergencyRequest {

        @Test
        @DisplayName("Should create emergency request successfully")
        void createEmergencyRequest_success() {
            EmergencyRequestCreateRequest createRequest = new EmergencyRequestCreateRequest(
                    hospitalId, bloodGroupId, componentTypeId, 4,
                    EmergencyPriorityEnum.EMERGENCY, "John Doe", "Trauma",
                    "Dr. Smith", Instant.now().plusSeconds(3600), null, null, branchId);

            when(emergencyRequestMapper.toEntity(createRequest)).thenReturn(emergencyRequest);
            when(emergencyRequestRepository.save(any(EmergencyRequest.class))).thenReturn(emergencyRequest);
            when(emergencyRequestMapper.toResponse(emergencyRequest)).thenReturn(emergencyRequestResponse);

            EmergencyRequestResponse result = emergencyService.createEmergencyRequest(createRequest);

            assertThat(result).isNotNull();
            assertThat(result.priority()).isEqualTo(EmergencyPriorityEnum.EMERGENCY);
            assertThat(result.status()).isEqualTo(EmergencyStatusEnum.OPEN);
            verify(emergencyRequestRepository).save(any(EmergencyRequest.class));
            verify(eventPublisher).publishEmergencyRequest(any());
        }

        @Test
        @DisplayName("Should set OPEN status and zero units fulfilled on creation")
        void createEmergencyRequest_setsDefaults() {
            EmergencyRequestCreateRequest createRequest = new EmergencyRequestCreateRequest(
                    hospitalId, bloodGroupId, componentTypeId, 2,
                    EmergencyPriorityEnum.CRITICAL, "Jane Doe", null,
                    "Dr. Lee", Instant.now().plusSeconds(1800), null, null, branchId);

            EmergencyRequest savedEntity = new EmergencyRequest();
            savedEntity.setStatus(EmergencyStatusEnum.OPEN);
            savedEntity.setUnitsFulfilled(0);
            savedEntity.setBroadcastSent(false);
            savedEntity.setBranchId(branchId);
            savedEntity.setBloodGroupId(bloodGroupId);
            savedEntity.setPriority(EmergencyPriorityEnum.CRITICAL);
            savedEntity.setRequestNumber("ER-TEST1234");

            when(emergencyRequestMapper.toEntity(createRequest)).thenReturn(new EmergencyRequest());
            when(emergencyRequestRepository.save(any(EmergencyRequest.class))).thenReturn(savedEntity);

            EmergencyRequestResponse resp = new EmergencyRequestResponse(
                    requestId, "ER-TEST1234", hospitalId, bloodGroupId, componentTypeId,
                    2, 0, EmergencyPriorityEnum.CRITICAL, "Jane Doe", null,
                    "Dr. Lee", Instant.now().plusSeconds(1800), EmergencyStatusEnum.OPEN,
                    false, null, null, branchId,
                    LocalDateTime.now(), LocalDateTime.now());
            when(emergencyRequestMapper.toResponse(savedEntity)).thenReturn(resp);

            EmergencyRequestResponse result = emergencyService.createEmergencyRequest(createRequest);

            assertThat(result.unitsFulfilled()).isEqualTo(0);
            assertThat(result.broadcastSent()).isFalse();
        }

        @Test
        @DisplayName("Should create O-negative emergency protocol request with CRITICAL priority")
        void createEmergencyRequest_oNegativeProtocol() {
            UUID oNegBloodGroupId = UUID.randomUUID();
            EmergencyRequestCreateRequest createRequest = new EmergencyRequestCreateRequest(
                    hospitalId, oNegBloodGroupId, componentTypeId, 6,
                    EmergencyPriorityEnum.CRITICAL, "Trauma Patient", "Massive hemorrhage",
                    "Dr. Emergency", Instant.now().plusSeconds(600), null, "O-negative protocol", branchId);

            EmergencyRequest oNegEntity = new EmergencyRequest();
            oNegEntity.setBloodGroupId(oNegBloodGroupId);
            oNegEntity.setPriority(EmergencyPriorityEnum.CRITICAL);
            oNegEntity.setBranchId(branchId);
            oNegEntity.setRequestNumber("ER-ONEG1234");

            when(emergencyRequestMapper.toEntity(createRequest)).thenReturn(oNegEntity);
            when(emergencyRequestRepository.save(any(EmergencyRequest.class))).thenReturn(oNegEntity);

            EmergencyRequestResponse resp = new EmergencyRequestResponse(
                    requestId, "ER-ONEG1234", hospitalId, oNegBloodGroupId, componentTypeId,
                    6, 0, EmergencyPriorityEnum.CRITICAL, "Trauma Patient", "Massive hemorrhage",
                    "Dr. Emergency", Instant.now().plusSeconds(600), EmergencyStatusEnum.OPEN,
                    false, null, "O-negative protocol", branchId,
                    LocalDateTime.now(), LocalDateTime.now());
            when(emergencyRequestMapper.toResponse(oNegEntity)).thenReturn(resp);

            EmergencyRequestResponse result = emergencyService.createEmergencyRequest(createRequest);

            assertThat(result.priority()).isEqualTo(EmergencyPriorityEnum.CRITICAL);
            assertThat(result.notes()).isEqualTo("O-negative protocol");
            assertThat(result.unitsNeeded()).isEqualTo(6);
            verify(eventPublisher).publishEmergencyRequest(any());
        }
    }

    @Nested
    @DisplayName("getById")
    class GetById {

        @Test
        @DisplayName("Should return emergency request when found")
        void getById_success() {
            when(emergencyRequestRepository.findById(requestId)).thenReturn(Optional.of(emergencyRequest));
            when(emergencyRequestMapper.toResponse(emergencyRequest)).thenReturn(emergencyRequestResponse);

            EmergencyRequestResponse result = emergencyService.getById(requestId);

            assertThat(result).isNotNull();
            assertThat(result.requestNumber()).isEqualTo("ER-ABCD1234");
        }

        @Test
        @DisplayName("Should throw when emergency request not found")
        void getById_notFound() {
            when(emergencyRequestRepository.findById(requestId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> emergencyService.getById(requestId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getByRequestNumber")
    class GetByRequestNumber {

        @Test
        @DisplayName("Should return emergency request when found by number")
        void getByRequestNumber_success() {
            when(emergencyRequestRepository.findByRequestNumber("ER-ABCD1234"))
                    .thenReturn(Optional.of(emergencyRequest));
            when(emergencyRequestMapper.toResponse(emergencyRequest)).thenReturn(emergencyRequestResponse);

            EmergencyRequestResponse result = emergencyService.getByRequestNumber("ER-ABCD1234");

            assertThat(result.requestNumber()).isEqualTo("ER-ABCD1234");
        }

        @Test
        @DisplayName("Should throw when request number not found")
        void getByRequestNumber_notFound() {
            when(emergencyRequestRepository.findByRequestNumber("ER-NOTEXIST"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> emergencyService.getByRequestNumber("ER-NOTEXIST"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getByStatus")
    class GetByStatus {

        @Test
        @DisplayName("Should return paged emergency requests by status")
        void getByStatus_success() {
            Pageable pageable = PageRequest.of(0, 20);
            Page<EmergencyRequest> page = new PageImpl<>(List.of(emergencyRequest), pageable, 1);
            when(emergencyRequestRepository.findByStatus(EmergencyStatusEnum.OPEN, pageable))
                    .thenReturn(page);
            when(emergencyRequestMapper.toResponseList(any()))
                    .thenReturn(List.of(emergencyRequestResponse));

            PagedResponse<EmergencyRequestResponse> result = emergencyService.getByStatus(
                    EmergencyStatusEnum.OPEN, pageable);

            assertThat(result.totalElements()).isEqualTo(1);
            assertThat(result.content()).hasSize(1);
        }

        @Test
        @DisplayName("Should return empty page when no matching requests")
        void getByStatus_empty() {
            Pageable pageable = PageRequest.of(0, 20);
            Page<EmergencyRequest> emptyPage = new PageImpl<>(List.of(), pageable, 0);
            when(emergencyRequestRepository.findByStatus(EmergencyStatusEnum.FULFILLED, pageable))
                    .thenReturn(emptyPage);
            when(emergencyRequestMapper.toResponseList(any())).thenReturn(List.of());

            PagedResponse<EmergencyRequestResponse> result = emergencyService.getByStatus(
                    EmergencyStatusEnum.FULFILLED, pageable);

            assertThat(result.totalElements()).isEqualTo(0);
            assertThat(result.content()).isEmpty();
        }
    }

    @Nested
    @DisplayName("escalate")
    class Escalate {

        @Test
        @DisplayName("Should escalate from EMERGENCY to CRITICAL")
        void escalate_emergencyToCritical() {
            emergencyRequest.setPriority(EmergencyPriorityEnum.EMERGENCY);
            when(emergencyRequestRepository.findById(requestId)).thenReturn(Optional.of(emergencyRequest));
            when(emergencyRequestRepository.save(any(EmergencyRequest.class))).thenReturn(emergencyRequest);

            EmergencyRequestResponse escalatedResponse = new EmergencyRequestResponse(
                    requestId, "ER-ABCD1234", hospitalId, bloodGroupId, componentTypeId,
                    4, 0, EmergencyPriorityEnum.CRITICAL, "John Doe", null,
                    null, Instant.now().plusSeconds(3600), EmergencyStatusEnum.OPEN,
                    false, null, null, branchId,
                    LocalDateTime.now(), LocalDateTime.now());
            when(emergencyRequestMapper.toResponse(emergencyRequest)).thenReturn(escalatedResponse);

            EmergencyRequestResponse result = emergencyService.escalate(requestId);

            assertThat(result.priority()).isEqualTo(EmergencyPriorityEnum.CRITICAL);
            verify(emergencyRequestRepository).save(any(EmergencyRequest.class));
            verify(eventPublisher).publishEmergencyRequest(any());
        }

        @Test
        @DisplayName("Should escalate from CRITICAL to MASS_CASUALTY")
        void escalate_criticalToMassCasualty() {
            emergencyRequest.setPriority(EmergencyPriorityEnum.CRITICAL);
            when(emergencyRequestRepository.findById(requestId)).thenReturn(Optional.of(emergencyRequest));
            when(emergencyRequestRepository.save(any(EmergencyRequest.class))).thenReturn(emergencyRequest);

            EmergencyRequestResponse escalatedResponse = new EmergencyRequestResponse(
                    requestId, "ER-ABCD1234", hospitalId, bloodGroupId, componentTypeId,
                    4, 0, EmergencyPriorityEnum.MASS_CASUALTY, "John Doe", null,
                    null, Instant.now().plusSeconds(3600), EmergencyStatusEnum.OPEN,
                    false, null, null, branchId,
                    LocalDateTime.now(), LocalDateTime.now());
            when(emergencyRequestMapper.toResponse(emergencyRequest)).thenReturn(escalatedResponse);

            EmergencyRequestResponse result = emergencyService.escalate(requestId);

            assertThat(result.priority()).isEqualTo(EmergencyPriorityEnum.MASS_CASUALTY);
            verify(eventPublisher).publishEmergencyRequest(any());
        }

        @Test
        @DisplayName("Should not change priority when already MASS_CASUALTY")
        void escalate_alreadyMassCasualty_noChange() {
            emergencyRequest.setPriority(EmergencyPriorityEnum.MASS_CASUALTY);
            when(emergencyRequestRepository.findById(requestId)).thenReturn(Optional.of(emergencyRequest));
            when(emergencyRequestRepository.save(any(EmergencyRequest.class))).thenReturn(emergencyRequest);
            when(emergencyRequestMapper.toResponse(emergencyRequest)).thenReturn(emergencyRequestResponse);

            EmergencyRequestResponse result = emergencyService.escalate(requestId);

            assertThat(result).isNotNull();
            verify(emergencyRequestRepository).save(any(EmergencyRequest.class));
        }

        @Test
        @DisplayName("Should throw when escalating non-existent request")
        void escalate_notFound() {
            when(emergencyRequestRepository.findById(requestId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> emergencyService.escalate(requestId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("cancel")
    class Cancel {

        @Test
        @DisplayName("Should cancel emergency request successfully")
        void cancel_success() {
            when(emergencyRequestRepository.findById(requestId)).thenReturn(Optional.of(emergencyRequest));
            when(emergencyRequestRepository.save(any(EmergencyRequest.class))).thenReturn(emergencyRequest);

            EmergencyRequestResponse cancelledResponse = new EmergencyRequestResponse(
                    requestId, "ER-ABCD1234", hospitalId, bloodGroupId, componentTypeId,
                    4, 0, EmergencyPriorityEnum.EMERGENCY, "John Doe", null,
                    null, Instant.now().plusSeconds(3600), EmergencyStatusEnum.CANCELLED,
                    false, null, null, branchId,
                    LocalDateTime.now(), LocalDateTime.now());
            when(emergencyRequestMapper.toResponse(emergencyRequest)).thenReturn(cancelledResponse);

            EmergencyRequestResponse result = emergencyService.cancel(requestId);

            assertThat(result.status()).isEqualTo(EmergencyStatusEnum.CANCELLED);
            verify(emergencyRequestRepository).save(any(EmergencyRequest.class));
        }

        @Test
        @DisplayName("Should throw when cancelling non-existent request")
        void cancel_notFound() {
            when(emergencyRequestRepository.findById(requestId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> emergencyService.cancel(requestId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("markBroadcastSent")
    class MarkBroadcastSent {

        @Test
        @DisplayName("Should mark broadcast as sent successfully")
        void markBroadcastSent_success() {
            when(emergencyRequestRepository.findById(requestId)).thenReturn(Optional.of(emergencyRequest));
            when(emergencyRequestRepository.save(any(EmergencyRequest.class))).thenReturn(emergencyRequest);

            EmergencyRequestResponse broadcastResponse = new EmergencyRequestResponse(
                    requestId, "ER-ABCD1234", hospitalId, bloodGroupId, componentTypeId,
                    4, 0, EmergencyPriorityEnum.EMERGENCY, "John Doe", null,
                    null, Instant.now().plusSeconds(3600), EmergencyStatusEnum.OPEN,
                    true, null, null, branchId,
                    LocalDateTime.now(), LocalDateTime.now());
            when(emergencyRequestMapper.toResponse(emergencyRequest)).thenReturn(broadcastResponse);

            EmergencyRequestResponse result = emergencyService.markBroadcastSent(requestId);

            assertThat(result.broadcastSent()).isTrue();
            verify(emergencyRequestRepository).save(any(EmergencyRequest.class));
        }

        @Test
        @DisplayName("Should throw when request not found for broadcast")
        void markBroadcastSent_notFound() {
            when(emergencyRequestRepository.findById(requestId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> emergencyService.markBroadcastSent(requestId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getByHospital")
    class GetByHospital {

        @Test
        @DisplayName("Should return emergency requests for a hospital")
        void getByHospital_success() {
            when(emergencyRequestRepository.findByHospitalId(hospitalId))
                    .thenReturn(List.of(emergencyRequest));
            when(emergencyRequestMapper.toResponseList(List.of(emergencyRequest)))
                    .thenReturn(List.of(emergencyRequestResponse));

            List<EmergencyRequestResponse> result = emergencyService.getByHospital(hospitalId);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should return empty list when no requests for hospital")
        void getByHospital_empty() {
            when(emergencyRequestRepository.findByHospitalId(hospitalId)).thenReturn(List.of());
            when(emergencyRequestMapper.toResponseList(List.of())).thenReturn(List.of());

            List<EmergencyRequestResponse> result = emergencyService.getByHospital(hospitalId);

            assertThat(result).isEmpty();
        }
    }
}
