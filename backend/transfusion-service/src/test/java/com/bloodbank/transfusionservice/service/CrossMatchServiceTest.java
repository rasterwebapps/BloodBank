package com.bloodbank.transfusionservice.service;

import com.bloodbank.common.dto.PagedResponse;
import com.bloodbank.common.exceptions.ResourceNotFoundException;
import com.bloodbank.common.model.enums.RequestStatusEnum;
import com.bloodbank.transfusionservice.dto.CrossMatchRequestCreateRequest;
import com.bloodbank.transfusionservice.dto.CrossMatchRequestResponse;
import com.bloodbank.transfusionservice.dto.CrossMatchResultCreateRequest;
import com.bloodbank.transfusionservice.dto.CrossMatchResultResponse;
import com.bloodbank.transfusionservice.entity.CrossMatchRequest;
import com.bloodbank.transfusionservice.entity.CrossMatchResult;
import com.bloodbank.transfusionservice.enums.CrossMatchMethodEnum;
import com.bloodbank.transfusionservice.enums.CrossMatchResultEnum;
import com.bloodbank.transfusionservice.enums.PriorityEnum;
import com.bloodbank.transfusionservice.mapper.CrossMatchRequestMapper;
import com.bloodbank.transfusionservice.mapper.CrossMatchResultMapper;
import com.bloodbank.transfusionservice.repository.CrossMatchRequestRepository;
import com.bloodbank.transfusionservice.repository.CrossMatchResultRepository;

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
class CrossMatchServiceTest {

    @Mock private CrossMatchRequestRepository requestRepository;
    @Mock private CrossMatchResultRepository resultRepository;
    @Mock private CrossMatchRequestMapper requestMapper;
    @Mock private CrossMatchResultMapper resultMapper;
    @InjectMocks private CrossMatchService crossMatchService;

    private UUID requestId;
    private UUID branchId;
    private UUID componentTypeId;
    private UUID bloodGroupId;
    private CrossMatchRequest crossMatchRequest;
    private CrossMatchRequestResponse crossMatchRequestResponse;

    @BeforeEach
    void setUp() {
        requestId = UUID.randomUUID();
        branchId = UUID.randomUUID();
        componentTypeId = UUID.randomUUID();
        bloodGroupId = UUID.randomUUID();

        crossMatchRequest = new CrossMatchRequest();
        crossMatchRequest.setRequestNumber("CM-ABCD1234");
        crossMatchRequest.setPatientName("John Doe");
        crossMatchRequest.setPatientId("PAT-001");
        crossMatchRequest.setPatientBloodGroupId(bloodGroupId);
        crossMatchRequest.setRequestingDoctor("Dr. Smith");
        crossMatchRequest.setUnitsRequested(2);
        crossMatchRequest.setComponentTypeId(componentTypeId);
        crossMatchRequest.setPriority(PriorityEnum.ROUTINE);
        crossMatchRequest.setStatus(RequestStatusEnum.PENDING);
        crossMatchRequest.setBranchId(branchId);

        crossMatchRequestResponse = new CrossMatchRequestResponse(
                requestId, "CM-ABCD1234", "John Doe", "PAT-001",
                bloodGroupId, null, "Dr. Smith", null, null,
                2, componentTypeId, PriorityEnum.ROUTINE, null,
                RequestStatusEnum.PENDING, null, branchId,
                LocalDateTime.now(), LocalDateTime.now());
    }

    @Nested @DisplayName("createRequest")
    class CreateRequest {
        @Test @DisplayName("Should create crossmatch request successfully")
        void createRequest_success() {
            var createReq = new CrossMatchRequestCreateRequest(
                    "John Doe", "PAT-001", bloodGroupId, null,
                    "Dr. Smith", null, null, 2, componentTypeId,
                    PriorityEnum.ROUTINE, null, null, branchId);
            when(requestMapper.toEntity(createReq)).thenReturn(crossMatchRequest);
            when(requestRepository.save(any(CrossMatchRequest.class))).thenReturn(crossMatchRequest);
            when(requestMapper.toResponse(crossMatchRequest)).thenReturn(crossMatchRequestResponse);

            var result = crossMatchService.createRequest(createReq);
            assertThat(result).isNotNull();
            assertThat(result.patientName()).isEqualTo("John Doe");
            verify(requestRepository).save(any(CrossMatchRequest.class));
        }
    }

    @Nested @DisplayName("addResult")
    class AddResult {
        @Test @DisplayName("Should add crossmatch result successfully")
        void addResult_success() {
            UUID componentId = UUID.randomUUID();
            var createReq = new CrossMatchResultCreateRequest(
                    requestId, componentId, CrossMatchMethodEnum.GEL_CARD,
                    CrossMatchResultEnum.COMPATIBLE, "Tech A", "Tech B", null, branchId);
            var entity = new CrossMatchResult();
            entity.setCrossmatchRequestId(requestId);
            entity.setResult(CrossMatchResultEnum.COMPATIBLE);
            var resp = new CrossMatchResultResponse(UUID.randomUUID(), requestId, componentId,
                    CrossMatchMethodEnum.GEL_CARD, CrossMatchResultEnum.COMPATIBLE,
                    "Tech A", "Tech B", Instant.now(), null, branchId,
                    LocalDateTime.now(), LocalDateTime.now());

            when(requestRepository.findById(requestId)).thenReturn(Optional.of(crossMatchRequest));
            when(resultMapper.toEntity(createReq)).thenReturn(entity);
            when(resultRepository.save(any(CrossMatchResult.class))).thenReturn(entity);
            when(resultMapper.toResponse(entity)).thenReturn(resp);

            var result = crossMatchService.addResult(createReq);
            assertThat(result.result()).isEqualTo(CrossMatchResultEnum.COMPATIBLE);
            verify(requestRepository).save(crossMatchRequest);
        }

        @Test @DisplayName("Should throw when request not found")
        void addResult_requestNotFound() {
            var createReq = new CrossMatchResultCreateRequest(
                    requestId, UUID.randomUUID(), CrossMatchMethodEnum.GEL_CARD,
                    CrossMatchResultEnum.COMPATIBLE, "Tech A", null, null, branchId);
            when(requestRepository.findById(requestId)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> crossMatchService.addResult(createReq))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested @DisplayName("getRequestById")
    class GetRequestById {
        @Test @DisplayName("Should return request when found")
        void getRequestById_success() {
            when(requestRepository.findById(requestId)).thenReturn(Optional.of(crossMatchRequest));
            when(requestMapper.toResponse(crossMatchRequest)).thenReturn(crossMatchRequestResponse);
            var result = crossMatchService.getRequestById(requestId);
            assertThat(result.patientName()).isEqualTo("John Doe");
        }

        @Test @DisplayName("Should throw when not found")
        void getRequestById_notFound() {
            when(requestRepository.findById(requestId)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> crossMatchService.getRequestById(requestId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested @DisplayName("getResultsByRequestId")
    class GetResultsByRequestId {
        @Test @DisplayName("Should return results for request")
        void success() {
            when(requestRepository.findById(requestId)).thenReturn(Optional.of(crossMatchRequest));
            when(resultRepository.findByCrossmatchRequestId(requestId)).thenReturn(List.of());
            when(resultMapper.toResponseList(any())).thenReturn(List.of());
            var results = crossMatchService.getResultsByRequestId(requestId);
            assertThat(results).isNotNull();
        }

        @Test @DisplayName("Should throw when request not found")
        void notFound() {
            when(requestRepository.findById(requestId)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> crossMatchService.getResultsByRequestId(requestId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested @DisplayName("getRequestsByStatus")
    class GetRequestsByStatus {
        @Test @DisplayName("Should return paged requests by status")
        void success() {
            Pageable pageable = PageRequest.of(0, 20);
            Page<CrossMatchRequest> page = new PageImpl<>(List.of(crossMatchRequest), pageable, 1);
            when(requestRepository.findByStatus(RequestStatusEnum.PENDING, pageable)).thenReturn(page);
            when(requestMapper.toResponseList(any())).thenReturn(List.of(crossMatchRequestResponse));
            var result = crossMatchService.getRequestsByStatus(RequestStatusEnum.PENDING, pageable);
            assertThat(result.totalElements()).isEqualTo(1);
        }
    }
}
