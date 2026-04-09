package com.bloodbank.complianceservice.service;

import com.bloodbank.common.exceptions.BusinessException;
import com.bloodbank.common.exceptions.ResourceNotFoundException;
import com.bloodbank.complianceservice.dto.RecallCreateRequest;
import com.bloodbank.complianceservice.dto.RecallResponse;
import com.bloodbank.complianceservice.entity.RecallRecord;
import com.bloodbank.complianceservice.enums.RecallSeverityEnum;
import com.bloodbank.complianceservice.enums.RecallStatusEnum;
import com.bloodbank.complianceservice.enums.RecallTypeEnum;
import com.bloodbank.complianceservice.event.EventPublisher;
import com.bloodbank.complianceservice.mapper.RecallRecordMapper;
import com.bloodbank.complianceservice.repository.RecallRecordRepository;

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
class RecallServiceTest {

    @Mock
    private RecallRecordRepository recallRepository;

    @Mock
    private RecallRecordMapper recallMapper;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private RecallService recallService;

    private UUID recallId;
    private UUID branchId;
    private RecallRecord recall;
    private RecallResponse recallResponse;
    private RecallCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        recallId = UUID.randomUUID();
        branchId = UUID.randomUUID();

        recall = new RecallRecord();
        recall.setId(recallId);
        recall.setRecallNumber("RCL-12345678");
        recall.setRecallType(RecallTypeEnum.PRODUCT_RECALL);
        recall.setRecallReason("Contamination detected");
        recall.setSeverity(RecallSeverityEnum.CLASS_I);
        recall.setInitiatedDate(Instant.now());
        recall.setAffectedUnitsCount(5);
        recall.setStatus(RecallStatusEnum.INITIATED);
        recall.setBranchId(branchId);

        recallResponse = new RecallResponse(
                recallId, "RCL-12345678", RecallTypeEnum.PRODUCT_RECALL,
                "Contamination detected", RecallSeverityEnum.CLASS_I,
                Instant.now(), null, 5, 0, 0, false, null,
                null, null, RecallStatusEnum.INITIATED, null, branchId,
                LocalDateTime.now(), LocalDateTime.now()
        );

        createRequest = new RecallCreateRequest(
                RecallTypeEnum.PRODUCT_RECALL, "Contamination detected",
                RecallSeverityEnum.CLASS_I, null, 5, null, null, branchId
        );
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("should create recall and publish event")
        void shouldCreateRecallAndPublishEvent() {
            when(recallMapper.toEntity(createRequest)).thenReturn(recall);
            when(recallRepository.save(any(RecallRecord.class))).thenReturn(recall);
            when(recallMapper.toResponse(recall)).thenReturn(recallResponse);

            RecallResponse result = recallService.create(createRequest);

            assertThat(result).isNotNull();
            assertThat(result.recallType()).isEqualTo(RecallTypeEnum.PRODUCT_RECALL);
            verify(recallRepository).save(any(RecallRecord.class));
            verify(eventPublisher).publishRecallInitiated(any());
        }

        @Test
        @DisplayName("should set INITIATED status and generate recall number")
        void shouldSetInitiatedStatusAndNumber() {
            RecallRecord newRecall = new RecallRecord();
            newRecall.setId(recallId);
            newRecall.setBranchId(branchId);
            newRecall.setRecallReason("Contamination detected");

            when(recallMapper.toEntity(createRequest)).thenReturn(newRecall);
            when(recallRepository.save(any(RecallRecord.class))).thenReturn(newRecall);
            when(recallMapper.toResponse(newRecall)).thenReturn(recallResponse);

            recallService.create(createRequest);

            assertThat(newRecall.getStatus()).isEqualTo(RecallStatusEnum.INITIATED);
            assertThat(newRecall.getRecallNumber()).startsWith("RCL-");
            assertThat(newRecall.getInitiatedDate()).isNotNull();
        }
    }

    @Nested
    @DisplayName("getById")
    class GetById {

        @Test
        @DisplayName("should return recall when found")
        void shouldReturnRecallWhenFound() {
            when(recallRepository.findById(recallId)).thenReturn(Optional.of(recall));
            when(recallMapper.toResponse(recall)).thenReturn(recallResponse);

            RecallResponse result = recallService.getById(recallId);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(recallId);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            when(recallRepository.findById(recallId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> recallService.getById(recallId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getByRecallNumber")
    class GetByRecallNumber {

        @Test
        @DisplayName("should return recall by number")
        void shouldReturnByNumber() {
            when(recallRepository.findByRecallNumber("RCL-12345678")).thenReturn(Optional.of(recall));
            when(recallMapper.toResponse(recall)).thenReturn(recallResponse);

            RecallResponse result = recallService.getByRecallNumber("RCL-12345678");

            assertThat(result.recallNumber()).isEqualTo("RCL-12345678");
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when number not found")
        void shouldThrowWhenNotFound() {
            when(recallRepository.findByRecallNumber("UNKNOWN")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> recallService.getByRecallNumber("UNKNOWN"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getByStatus")
    class GetByStatus {

        @Test
        @DisplayName("should return recalls by status")
        void shouldReturnByStatus() {
            List<RecallRecord> recalls = List.of(recall);
            List<RecallResponse> responses = List.of(recallResponse);
            when(recallRepository.findByStatus(RecallStatusEnum.INITIATED)).thenReturn(recalls);
            when(recallMapper.toResponseList(recalls)).thenReturn(responses);

            List<RecallResponse> result = recallService.getByStatus(RecallStatusEnum.INITIATED);

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getByType")
    class GetByType {

        @Test
        @DisplayName("should return recalls by type")
        void shouldReturnByType() {
            List<RecallRecord> recalls = List.of(recall);
            List<RecallResponse> responses = List.of(recallResponse);
            when(recallRepository.findByRecallType(RecallTypeEnum.PRODUCT_RECALL)).thenReturn(recalls);
            when(recallMapper.toResponseList(recalls)).thenReturn(responses);

            List<RecallResponse> result = recallService.getByType(RecallTypeEnum.PRODUCT_RECALL);

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("updateStatus")
    class UpdateStatus {

        @Test
        @DisplayName("should update status successfully")
        void shouldUpdateStatusSuccessfully() {
            when(recallRepository.findById(recallId)).thenReturn(Optional.of(recall));
            when(recallRepository.save(any(RecallRecord.class))).thenReturn(recall);
            when(recallMapper.toResponse(recall)).thenReturn(recallResponse);

            RecallResponse result = recallService.updateStatus(recallId, RecallStatusEnum.IN_PROGRESS);

            assertThat(result).isNotNull();
            verify(recallRepository).save(any(RecallRecord.class));
        }

        @Test
        @DisplayName("should throw BusinessException when recall is closed")
        void shouldThrowWhenClosed() {
            recall.setStatus(RecallStatusEnum.CLOSED);
            when(recallRepository.findById(recallId)).thenReturn(Optional.of(recall));

            assertThatThrownBy(() -> recallService.updateStatus(recallId, RecallStatusEnum.IN_PROGRESS))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            when(recallRepository.findById(recallId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> recallService.updateStatus(recallId, RecallStatusEnum.IN_PROGRESS))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("close")
    class CloseRecall {

        @Test
        @DisplayName("should close recall successfully")
        void shouldCloseRecallSuccessfully() {
            when(recallRepository.findById(recallId)).thenReturn(Optional.of(recall));
            when(recallRepository.save(any(RecallRecord.class))).thenReturn(recall);
            when(recallMapper.toResponse(recall)).thenReturn(recallResponse);

            recallService.close(recallId, "admin");

            assertThat(recall.getStatus()).isEqualTo(RecallStatusEnum.CLOSED);
            assertThat(recall.getClosedBy()).isEqualTo("admin");
            assertThat(recall.getClosureDate()).isNotNull();
        }

        @Test
        @DisplayName("should throw BusinessException when already closed")
        void shouldThrowWhenAlreadyClosed() {
            recall.setStatus(RecallStatusEnum.CLOSED);
            when(recallRepository.findById(recallId)).thenReturn(Optional.of(recall));

            assertThatThrownBy(() -> recallService.close(recallId, "admin"))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            when(recallRepository.findById(recallId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> recallService.close(recallId, "admin"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
