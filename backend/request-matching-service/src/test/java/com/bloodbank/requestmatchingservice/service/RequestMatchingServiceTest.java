package com.bloodbank.requestmatchingservice.service;

import com.bloodbank.common.exceptions.ResourceNotFoundException;
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
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestMatchingServiceTest {

    @Mock private EmergencyRequestRepository emergencyRequestRepository;
    @Mock private EmergencyRequestMapper emergencyRequestMapper;
    @Mock private RequestMatchingEventPublisher eventPublisher;
    @InjectMocks private RequestMatchingService requestMatchingService;

    private UUID requestId;
    private UUID branchId;
    private UUID bloodGroupId;
    private UUID componentTypeId;
    private EmergencyRequest emergencyRequest;
    private EmergencyRequestResponse emergencyRequestResponse;

    @BeforeEach
    void setUp() {
        requestId = UUID.randomUUID();
        branchId = UUID.randomUUID();
        bloodGroupId = UUID.randomUUID();
        componentTypeId = UUID.randomUUID();

        emergencyRequest = new EmergencyRequest();
        emergencyRequest.setRequestNumber("ER-ABCD1234");
        emergencyRequest.setBloodGroupId(bloodGroupId);
        emergencyRequest.setComponentTypeId(componentTypeId);
        emergencyRequest.setUnitsNeeded(4);
        emergencyRequest.setUnitsFulfilled(0);
        emergencyRequest.setPriority(EmergencyPriorityEnum.EMERGENCY);
        emergencyRequest.setStatus(EmergencyStatusEnum.OPEN);
        emergencyRequest.setBranchId(branchId);
        emergencyRequest.setPatientName("John Doe");
        emergencyRequest.setRequiredBy(Instant.now().plusSeconds(3600));

        emergencyRequestResponse = new EmergencyRequestResponse(
                requestId, "ER-ABCD1234", null, bloodGroupId, componentTypeId,
                4, 0, EmergencyPriorityEnum.EMERGENCY, "John Doe", null,
                null, Instant.now().plusSeconds(3600), EmergencyStatusEnum.OPEN,
                false, null, null, branchId,
                LocalDateTime.now(), LocalDateTime.now());
    }

    @Nested
    @DisplayName("matchRequest")
    class MatchRequest {

        @Test
        @DisplayName("Should return request unchanged when no compatible units found (no-match scenario)")
        void matchRequest_noCompatibleUnits_returnsUnchanged() {
            when(emergencyRequestRepository.findById(requestId)).thenReturn(Optional.of(emergencyRequest));
            when(emergencyRequestMapper.toResponse(emergencyRequest)).thenReturn(emergencyRequestResponse);

            EmergencyRequestResponse result = requestMatchingService.matchRequest(requestId);

            assertThat(result).isNotNull();
            assertThat(result.status()).isEqualTo(EmergencyStatusEnum.OPEN);
            assertThat(result.unitsFulfilled()).isEqualTo(0);
            verify(emergencyRequestRepository, never()).save(any());
            verify(eventPublisher, never()).publishBloodRequestMatched(any());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when request not found")
        void matchRequest_notFound_throwsException() {
            when(emergencyRequestRepository.findById(requestId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> requestMatchingService.matchRequest(requestId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should return without matching when request is in FULFILLED status")
        void matchRequest_alreadyFulfilled_returnsWithoutMatching() {
            emergencyRequest.setStatus(EmergencyStatusEnum.FULFILLED);
            EmergencyRequestResponse fulfilledResponse = new EmergencyRequestResponse(
                    requestId, "ER-ABCD1234", null, bloodGroupId, componentTypeId,
                    4, 4, EmergencyPriorityEnum.EMERGENCY, "John Doe", null,
                    null, Instant.now().plusSeconds(3600), EmergencyStatusEnum.FULFILLED,
                    false, null, null, branchId,
                    LocalDateTime.now(), LocalDateTime.now());

            when(emergencyRequestRepository.findById(requestId)).thenReturn(Optional.of(emergencyRequest));
            when(emergencyRequestMapper.toResponse(emergencyRequest)).thenReturn(fulfilledResponse);

            EmergencyRequestResponse result = requestMatchingService.matchRequest(requestId);

            assertThat(result.status()).isEqualTo(EmergencyStatusEnum.FULFILLED);
            verify(emergencyRequestRepository, never()).save(any());
            verify(eventPublisher, never()).publishBloodRequestMatched(any());
        }

        @Test
        @DisplayName("Should return without matching when request is CANCELLED")
        void matchRequest_cancelled_returnsWithoutMatching() {
            emergencyRequest.setStatus(EmergencyStatusEnum.CANCELLED);
            EmergencyRequestResponse cancelledResponse = new EmergencyRequestResponse(
                    requestId, "ER-ABCD1234", null, bloodGroupId, componentTypeId,
                    4, 0, EmergencyPriorityEnum.EMERGENCY, "John Doe", null,
                    null, Instant.now().plusSeconds(3600), EmergencyStatusEnum.CANCELLED,
                    false, null, null, branchId,
                    LocalDateTime.now(), LocalDateTime.now());

            when(emergencyRequestRepository.findById(requestId)).thenReturn(Optional.of(emergencyRequest));
            when(emergencyRequestMapper.toResponse(emergencyRequest)).thenReturn(cancelledResponse);

            EmergencyRequestResponse result = requestMatchingService.matchRequest(requestId);

            assertThat(result.status()).isEqualTo(EmergencyStatusEnum.CANCELLED);
            verify(emergencyRequestRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should allow matching when request is PARTIALLY_FULFILLED")
        void matchRequest_partiallyFulfilled_attemptsMatch() {
            emergencyRequest.setStatus(EmergencyStatusEnum.PARTIALLY_FULFILLED);
            emergencyRequest.setUnitsFulfilled(2);
            EmergencyRequestResponse partialResponse = new EmergencyRequestResponse(
                    requestId, "ER-ABCD1234", null, bloodGroupId, componentTypeId,
                    4, 2, EmergencyPriorityEnum.EMERGENCY, "John Doe", null,
                    null, Instant.now().plusSeconds(3600), EmergencyStatusEnum.PARTIALLY_FULFILLED,
                    false, null, null, branchId,
                    LocalDateTime.now(), LocalDateTime.now());

            when(emergencyRequestRepository.findById(requestId)).thenReturn(Optional.of(emergencyRequest));
            when(emergencyRequestMapper.toResponse(emergencyRequest)).thenReturn(partialResponse);

            EmergencyRequestResponse result = requestMatchingService.matchRequest(requestId);

            assertThat(result).isNotNull();
            // Since findCompatibleUnits returns empty list, no save or event
            verify(emergencyRequestRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should return without matching when request is EXPIRED")
        void matchRequest_expired_returnsWithoutMatching() {
            emergencyRequest.setStatus(EmergencyStatusEnum.EXPIRED);
            EmergencyRequestResponse expiredResponse = new EmergencyRequestResponse(
                    requestId, "ER-ABCD1234", null, bloodGroupId, componentTypeId,
                    4, 0, EmergencyPriorityEnum.EMERGENCY, "John Doe", null,
                    null, Instant.now().plusSeconds(3600), EmergencyStatusEnum.EXPIRED,
                    false, null, null, branchId,
                    LocalDateTime.now(), LocalDateTime.now());

            when(emergencyRequestRepository.findById(requestId)).thenReturn(Optional.of(emergencyRequest));
            when(emergencyRequestMapper.toResponse(emergencyRequest)).thenReturn(expiredResponse);

            EmergencyRequestResponse result = requestMatchingService.matchRequest(requestId);

            assertThat(result.status()).isEqualTo(EmergencyStatusEnum.EXPIRED);
            verify(emergencyRequestRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getOpenRequests")
    class GetOpenRequests {

        @Test
        @DisplayName("Should return all open emergency requests")
        void getOpenRequests_success() {
            Page<EmergencyRequest> page = new PageImpl<>(List.of(emergencyRequest));
            when(emergencyRequestRepository.findByStatus(EmergencyStatusEnum.OPEN, Pageable.unpaged()))
                    .thenReturn(page);
            when(emergencyRequestMapper.toResponseList(List.of(emergencyRequest)))
                    .thenReturn(List.of(emergencyRequestResponse));

            List<EmergencyRequestResponse> result = requestMatchingService.getOpenRequests();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).requestNumber()).isEqualTo("ER-ABCD1234");
        }

        @Test
        @DisplayName("Should return empty list when no open requests")
        void getOpenRequests_empty() {
            Page<EmergencyRequest> emptyPage = new PageImpl<>(List.of());
            when(emergencyRequestRepository.findByStatus(EmergencyStatusEnum.OPEN, Pageable.unpaged()))
                    .thenReturn(emptyPage);
            when(emergencyRequestMapper.toResponseList(List.of())).thenReturn(List.of());

            List<EmergencyRequestResponse> result = requestMatchingService.getOpenRequests();

            assertThat(result).isEmpty();
        }
    }
}
