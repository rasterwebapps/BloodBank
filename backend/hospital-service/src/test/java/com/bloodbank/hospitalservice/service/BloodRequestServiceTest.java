package com.bloodbank.hospitalservice.service;

import com.bloodbank.common.dto.PagedResponse;
import com.bloodbank.common.events.BloodRequestCreatedEvent;
import com.bloodbank.common.exceptions.ResourceNotFoundException;
import com.bloodbank.hospitalservice.dto.HospitalRequestCreateRequest;
import com.bloodbank.hospitalservice.dto.HospitalRequestResponse;
import com.bloodbank.hospitalservice.entity.Hospital;
import com.bloodbank.hospitalservice.entity.HospitalRequest;
import com.bloodbank.hospitalservice.enums.HospitalRequestStatusEnum;
import com.bloodbank.hospitalservice.enums.PriorityEnum;
import com.bloodbank.hospitalservice.event.HospitalEventPublisher;
import com.bloodbank.hospitalservice.mapper.HospitalRequestMapper;
import com.bloodbank.hospitalservice.repository.HospitalRepository;
import com.bloodbank.hospitalservice.repository.HospitalRequestRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BloodRequestServiceTest {

    @Mock
    private HospitalRequestRepository requestRepository;

    @Mock
    private HospitalRepository hospitalRepository;

    @Mock
    private HospitalRequestMapper requestMapper;

    @Mock
    private HospitalEventPublisher eventPublisher;

    @InjectMocks
    private BloodRequestService bloodRequestService;

    private UUID requestId;
    private UUID hospitalId;
    private UUID branchId;
    private UUID bloodGroupId;
    private UUID componentTypeId;
    private HospitalRequest hospitalRequest;
    private HospitalRequestResponse requestResponse;
    private HospitalRequestCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        requestId = UUID.randomUUID();
        hospitalId = UUID.randomUUID();
        branchId = UUID.randomUUID();
        bloodGroupId = UUID.randomUUID();
        componentTypeId = UUID.randomUUID();

        hospitalRequest = new HospitalRequest();
        hospitalRequest.setHospitalId(hospitalId);
        hospitalRequest.setRequestNumber("REQ-ABCD1234");
        hospitalRequest.setPatientName("Jane Doe");
        hospitalRequest.setPatientId("PAT-001");
        hospitalRequest.setPatientBloodGroupId(bloodGroupId);
        hospitalRequest.setComponentTypeId(componentTypeId);
        hospitalRequest.setUnitsRequested(3);
        hospitalRequest.setPriority(PriorityEnum.URGENT);
        hospitalRequest.setStatus(HospitalRequestStatusEnum.PENDING);
        hospitalRequest.setUnitsFulfilled(0);
        hospitalRequest.setBranchId(branchId);
        hospitalRequest.setRequestingDoctor("Dr. Johnson");
        hospitalRequest.setClinicalIndication("Surgery preparation");

        requestResponse = new HospitalRequestResponse(
                requestId, hospitalId, "REQ-ABCD1234",
                "Jane Doe", "PAT-001", bloodGroupId, componentTypeId,
                3, PriorityEnum.URGENT, Instant.now(),
                "Surgery preparation", null, "Dr. Johnson", "DOC-001",
                HospitalRequestStatusEnum.PENDING, 0, null, null,
                branchId, LocalDateTime.now(), LocalDateTime.now()
        );

        createRequest = new HospitalRequestCreateRequest(
                hospitalId, "Jane Doe", "PAT-001", bloodGroupId,
                componentTypeId, 3, PriorityEnum.URGENT,
                Instant.now(), "Surgery preparation", null,
                "Dr. Johnson", "DOC-001", null, branchId
        );
    }

    @Nested
    @DisplayName("createRequest")
    class CreateRequest {

        @Test
        @DisplayName("Should create blood request successfully")
        void shouldCreateRequestSuccessfully() {
            when(hospitalRepository.findById(hospitalId)).thenReturn(Optional.of(new Hospital()));
            when(requestMapper.toEntity(createRequest)).thenReturn(hospitalRequest);
            when(requestRepository.save(any(HospitalRequest.class))).thenReturn(hospitalRequest);
            when(requestMapper.toResponse(hospitalRequest)).thenReturn(requestResponse);

            HospitalRequestResponse result = bloodRequestService.createRequest(createRequest);

            assertThat(result).isNotNull();
            assertThat(result.patientName()).isEqualTo("Jane Doe");
            assertThat(result.status()).isEqualTo(HospitalRequestStatusEnum.PENDING);
            verify(requestRepository).save(any(HospitalRequest.class));
            verify(eventPublisher).publishBloodRequestCreated(any(BloodRequestCreatedEvent.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when hospital not found")
        void shouldThrowWhenHospitalNotFound() {
            when(hospitalRepository.findById(hospitalId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bloodRequestService.createRequest(createRequest))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(requestRepository, never()).save(any());
            verify(eventPublisher, never()).publishBloodRequestCreated(any());
        }

        @Test
        @DisplayName("Should set request number, status, units fulfilled, and branchId on creation")
        void shouldSetDefaultFieldsOnCreation() {
            HospitalRequest capturedRequest = new HospitalRequest();
            capturedRequest.setPatientBloodGroupId(bloodGroupId);
            capturedRequest.setHospitalId(hospitalId);
            capturedRequest.setBranchId(branchId);
            when(hospitalRepository.findById(hospitalId)).thenReturn(Optional.of(new Hospital()));
            when(requestMapper.toEntity(createRequest)).thenReturn(capturedRequest);
            when(requestRepository.save(any(HospitalRequest.class))).thenAnswer(inv -> inv.getArgument(0));
            when(requestMapper.toResponse(any(HospitalRequest.class))).thenReturn(requestResponse);

            bloodRequestService.createRequest(createRequest);

            assertThat(capturedRequest.getRequestNumber()).startsWith("REQ-");
            assertThat(capturedRequest.getStatus()).isEqualTo(HospitalRequestStatusEnum.PENDING);
            assertThat(capturedRequest.getUnitsFulfilled()).isEqualTo(0);
            assertThat(capturedRequest.getBranchId()).isEqualTo(branchId);
        }

        @Test
        @DisplayName("Should publish BloodRequestCreatedEvent after saving")
        void shouldPublishEvent() {
            when(hospitalRepository.findById(hospitalId)).thenReturn(Optional.of(new Hospital()));
            when(requestMapper.toEntity(createRequest)).thenReturn(hospitalRequest);
            when(requestRepository.save(any(HospitalRequest.class))).thenReturn(hospitalRequest);
            when(requestMapper.toResponse(hospitalRequest)).thenReturn(requestResponse);

            bloodRequestService.createRequest(createRequest);

            verify(eventPublisher).publishBloodRequestCreated(any(BloodRequestCreatedEvent.class));
        }
    }

    @Nested
    @DisplayName("getRequestById")
    class GetRequestById {

        @Test
        @DisplayName("Should return request when found")
        void shouldReturnRequestWhenFound() {
            when(requestRepository.findById(requestId)).thenReturn(Optional.of(hospitalRequest));
            when(requestMapper.toResponse(hospitalRequest)).thenReturn(requestResponse);

            HospitalRequestResponse result = bloodRequestService.getRequestById(requestId);

            assertThat(result).isNotNull();
            assertThat(result.patientName()).isEqualTo("Jane Doe");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            when(requestRepository.findById(requestId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bloodRequestService.getRequestById(requestId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getRequestByNumber")
    class GetRequestByNumber {

        @Test
        @DisplayName("Should return request when number found")
        void shouldReturnRequestByNumber() {
            when(requestRepository.findByRequestNumber("REQ-ABCD1234")).thenReturn(Optional.of(hospitalRequest));
            when(requestMapper.toResponse(hospitalRequest)).thenReturn(requestResponse);

            HospitalRequestResponse result = bloodRequestService.getRequestByNumber("REQ-ABCD1234");

            assertThat(result).isNotNull();
            assertThat(result.requestNumber()).isEqualTo("REQ-ABCD1234");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when number not found")
        void shouldThrowWhenNumberNotFound() {
            when(requestRepository.findByRequestNumber("INVALID")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bloodRequestService.getRequestByNumber("INVALID"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getRequestsByHospitalId")
    class GetRequestsByHospitalId {

        @Test
        @DisplayName("Should return paged requests for hospital")
        void shouldReturnPagedRequests() {
            Pageable pageable = PageRequest.of(0, 20);
            Page<HospitalRequest> page = new PageImpl<>(List.of(hospitalRequest), pageable, 1);
            when(requestRepository.findByHospitalId(hospitalId, pageable)).thenReturn(page);
            when(requestMapper.toResponseList(List.of(hospitalRequest))).thenReturn(List.of(requestResponse));

            PagedResponse<HospitalRequestResponse> result = bloodRequestService.getRequestsByHospitalId(hospitalId, pageable);

            assertThat(result.content()).hasSize(1);
            assertThat(result.totalElements()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("getRequestsByStatus")
    class GetRequestsByStatus {

        @Test
        @DisplayName("Should return paged requests by status")
        void shouldReturnRequestsByStatus() {
            Pageable pageable = PageRequest.of(0, 20);
            Page<HospitalRequest> page = new PageImpl<>(List.of(hospitalRequest), pageable, 1);
            when(requestRepository.findByStatus(HospitalRequestStatusEnum.PENDING, pageable)).thenReturn(page);
            when(requestMapper.toResponseList(List.of(hospitalRequest))).thenReturn(List.of(requestResponse));

            PagedResponse<HospitalRequestResponse> result = bloodRequestService.getRequestsByStatus(HospitalRequestStatusEnum.PENDING, pageable);

            assertThat(result.content()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getAllRequests")
    class GetAllRequests {

        @Test
        @DisplayName("Should return all paged requests")
        void shouldReturnAllRequests() {
            Pageable pageable = PageRequest.of(0, 20);
            Page<HospitalRequest> page = new PageImpl<>(List.of(hospitalRequest), pageable, 1);
            when(requestRepository.findAll(pageable)).thenReturn(page);
            when(requestMapper.toResponseList(List.of(hospitalRequest))).thenReturn(List.of(requestResponse));

            PagedResponse<HospitalRequestResponse> result = bloodRequestService.getAllRequests(pageable);

            assertThat(result.content()).hasSize(1);
            assertThat(result.totalElements()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("updateRequestStatus")
    class UpdateRequestStatus {

        @Test
        @DisplayName("Should update request status successfully")
        void shouldUpdateStatusSuccessfully() {
            when(requestRepository.findById(requestId)).thenReturn(Optional.of(hospitalRequest));
            when(requestRepository.save(any(HospitalRequest.class))).thenReturn(hospitalRequest);
            when(requestMapper.toResponse(hospitalRequest)).thenReturn(requestResponse);

            HospitalRequestResponse result = bloodRequestService.updateRequestStatus(
                    requestId, HospitalRequestStatusEnum.APPROVED, null);

            assertThat(result).isNotNull();
            verify(requestRepository).save(hospitalRequest);
        }

        @Test
        @DisplayName("Should set rejection reason when provided")
        void shouldSetRejectionReason() {
            when(requestRepository.findById(requestId)).thenReturn(Optional.of(hospitalRequest));
            when(requestRepository.save(any(HospitalRequest.class))).thenReturn(hospitalRequest);
            when(requestMapper.toResponse(hospitalRequest)).thenReturn(requestResponse);

            bloodRequestService.updateRequestStatus(
                    requestId, HospitalRequestStatusEnum.REJECTED, "Insufficient stock");

            assertThat(hospitalRequest.getStatus()).isEqualTo(HospitalRequestStatusEnum.REJECTED);
            assertThat(hospitalRequest.getRejectionReason()).isEqualTo("Insufficient stock");
        }

        @Test
        @DisplayName("Should not set rejection reason when null")
        void shouldNotSetRejectionReasonWhenNull() {
            when(requestRepository.findById(requestId)).thenReturn(Optional.of(hospitalRequest));
            when(requestRepository.save(any(HospitalRequest.class))).thenReturn(hospitalRequest);
            when(requestMapper.toResponse(hospitalRequest)).thenReturn(requestResponse);

            bloodRequestService.updateRequestStatus(
                    requestId, HospitalRequestStatusEnum.APPROVED, null);

            assertThat(hospitalRequest.getStatus()).isEqualTo(HospitalRequestStatusEnum.APPROVED);
            assertThat(hospitalRequest.getRejectionReason()).isNull();
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when request not found")
        void shouldThrowWhenRequestNotFound() {
            when(requestRepository.findById(requestId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bloodRequestService.updateRequestStatus(
                    requestId, HospitalRequestStatusEnum.APPROVED, null))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("cancelRequest")
    class CancelRequest {

        @Test
        @DisplayName("Should cancel request successfully")
        void shouldCancelRequestSuccessfully() {
            when(requestRepository.findById(requestId)).thenReturn(Optional.of(hospitalRequest));
            when(requestRepository.save(any(HospitalRequest.class))).thenReturn(hospitalRequest);
            when(requestMapper.toResponse(hospitalRequest)).thenReturn(requestResponse);

            HospitalRequestResponse result = bloodRequestService.cancelRequest(requestId);

            assertThat(result).isNotNull();
            assertThat(hospitalRequest.getStatus()).isEqualTo(HospitalRequestStatusEnum.CANCELLED);
            verify(requestRepository).save(hospitalRequest);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when request not found for cancellation")
        void shouldThrowWhenRequestNotFoundForCancellation() {
            when(requestRepository.findById(requestId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bloodRequestService.cancelRequest(requestId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
