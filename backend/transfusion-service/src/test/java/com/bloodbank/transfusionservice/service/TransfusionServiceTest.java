package com.bloodbank.transfusionservice.service;

import com.bloodbank.common.dto.PagedResponse;
import com.bloodbank.common.events.TransfusionCompletedEvent;
import com.bloodbank.common.events.TransfusionReactionEvent;
import com.bloodbank.common.exceptions.BusinessException;
import com.bloodbank.common.exceptions.ResourceNotFoundException;
import com.bloodbank.common.model.enums.SeverityEnum;
import com.bloodbank.common.model.enums.TransfusionStatusEnum;
import com.bloodbank.transfusionservice.dto.*;
import com.bloodbank.transfusionservice.entity.BloodIssue;
import com.bloodbank.transfusionservice.entity.Transfusion;
import com.bloodbank.transfusionservice.entity.TransfusionReaction;
import com.bloodbank.transfusionservice.enums.IssueStatusEnum;
import com.bloodbank.transfusionservice.enums.ReactionOutcomeEnum;
import com.bloodbank.transfusionservice.enums.TransfusionOutcomeEnum;
import com.bloodbank.transfusionservice.event.TransfusionEventPublisher;
import com.bloodbank.transfusionservice.mapper.TransfusionMapper;
import com.bloodbank.transfusionservice.mapper.TransfusionReactionMapper;
import com.bloodbank.transfusionservice.repository.BloodIssueRepository;
import com.bloodbank.transfusionservice.repository.TransfusionReactionRepository;
import com.bloodbank.transfusionservice.repository.TransfusionRepository;

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
class TransfusionServiceTest {

    @Mock private TransfusionRepository transfusionRepository;
    @Mock private TransfusionReactionRepository reactionRepository;
    @Mock private BloodIssueRepository bloodIssueRepository;
    @Mock private TransfusionMapper transfusionMapper;
    @Mock private TransfusionReactionMapper reactionMapper;
    @Mock private TransfusionEventPublisher eventPublisher;
    @InjectMocks private TransfusionService transfusionService;

    private UUID transfusionId;
    private UUID bloodIssueId;
    private UUID branchId;
    private Transfusion transfusion;
    private BloodIssue bloodIssue;
    private TransfusionResponse transfusionResponse;

    @BeforeEach
    void setUp() {
        transfusionId = UUID.randomUUID();
        bloodIssueId = UUID.randomUUID();
        branchId = UUID.randomUUID();

        bloodIssue = new BloodIssue();
        bloodIssue.setId(bloodIssueId);
        bloodIssue.setStatus(IssueStatusEnum.ISSUED);
        bloodIssue.setBranchId(branchId);

        transfusion = new Transfusion();
        transfusion.setId(transfusionId);
        transfusion.setBloodIssueId(bloodIssueId);
        transfusion.setPatientName("John Doe");
        transfusion.setPatientId("PAT-001");
        transfusion.setStatus(TransfusionStatusEnum.IN_PROGRESS);
        transfusion.setTransfusionStart(Instant.now());
        transfusion.setBranchId(branchId);

        transfusionResponse = new TransfusionResponse(
                transfusionId, bloodIssueId, "John Doe", "PAT-001",
                null, Instant.now(), null, null,
                "Nurse A", null, null, null,
                TransfusionStatusEnum.IN_PROGRESS, null, null, branchId,
                LocalDateTime.now(), LocalDateTime.now());
    }

    @Nested @DisplayName("startTransfusion")
    class StartTransfusion {
        @Test @DisplayName("Should start transfusion successfully")
        void success() {
            var request = new TransfusionCreateRequest(bloodIssueId, "John Doe", "PAT-001", null,
                    "Nurse A", null, null, null, branchId);
            when(bloodIssueRepository.findById(bloodIssueId)).thenReturn(Optional.of(bloodIssue));
            when(transfusionMapper.toEntity(request)).thenReturn(transfusion);
            when(transfusionRepository.save(any(Transfusion.class))).thenReturn(transfusion);
            when(transfusionMapper.toResponse(transfusion)).thenReturn(transfusionResponse);

            var result = transfusionService.startTransfusion(request);
            assertThat(result.patientName()).isEqualTo("John Doe");
        }

        @Test @DisplayName("Should throw when blood issue not found")
        void bloodIssueNotFound() {
            var request = new TransfusionCreateRequest(bloodIssueId, "John Doe", "PAT-001", null,
                    "Nurse A", null, null, null, branchId);
            when(bloodIssueRepository.findById(bloodIssueId)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> transfusionService.startTransfusion(request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test @DisplayName("Should throw when blood issue not in ISSUED status")
        void invalidStatus() {
            bloodIssue.setStatus(IssueStatusEnum.RETURNED);
            var request = new TransfusionCreateRequest(bloodIssueId, "John Doe", "PAT-001", null,
                    "Nurse A", null, null, null, branchId);
            when(bloodIssueRepository.findById(bloodIssueId)).thenReturn(Optional.of(bloodIssue));
            assertThatThrownBy(() -> transfusionService.startTransfusion(request))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested @DisplayName("completeTransfusion")
    class CompleteTransfusion {
        @Test @DisplayName("Should complete transfusion successfully")
        void success() {
            var request = new TransfusionCompleteRequest(450, "BP: 120/80", "Done");
            var completedResp = new TransfusionResponse(transfusionId, bloodIssueId, "John Doe", "PAT-001",
                    null, Instant.now(), Instant.now(), 450, "Nurse A", null, null, "BP: 120/80",
                    TransfusionStatusEnum.COMPLETED, TransfusionOutcomeEnum.SUCCESSFUL, "Done", branchId,
                    LocalDateTime.now(), LocalDateTime.now());

            when(transfusionRepository.findById(transfusionId)).thenReturn(Optional.of(transfusion));
            when(transfusionRepository.save(any(Transfusion.class))).thenReturn(transfusion);
            when(bloodIssueRepository.findById(bloodIssueId)).thenReturn(Optional.of(bloodIssue));
            when(transfusionMapper.toResponse(transfusion)).thenReturn(completedResp);

            var result = transfusionService.completeTransfusion(transfusionId, request);
            assertThat(result.status()).isEqualTo(TransfusionStatusEnum.COMPLETED);
            verify(eventPublisher).publishTransfusionCompleted(any(TransfusionCompletedEvent.class));
        }

        @Test @DisplayName("Should throw when not found")
        void notFound() {
            var request = new TransfusionCompleteRequest(450, null, null);
            when(transfusionRepository.findById(transfusionId)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> transfusionService.completeTransfusion(transfusionId, request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test @DisplayName("Should throw when not in progress")
        void invalidStatus() {
            transfusion.setStatus(TransfusionStatusEnum.COMPLETED);
            var request = new TransfusionCompleteRequest(450, null, null);
            when(transfusionRepository.findById(transfusionId)).thenReturn(Optional.of(transfusion));
            assertThatThrownBy(() -> transfusionService.completeTransfusion(transfusionId, request))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested @DisplayName("reportReaction")
    class ReportReaction {
        @Test @DisplayName("Should report reaction successfully")
        void success() {
            UUID reactionTypeId = UUID.randomUUID();
            var request = new TransfusionReactionCreateRequest(transfusionId, reactionTypeId, Instant.now(),
                    "Fever", SeverityEnum.MODERATE, "Paracetamol", ReactionOutcomeEnum.RESOLVED,
                    "Nurse A", branchId);
            var reaction = new TransfusionReaction();
            reaction.setTransfusionId(transfusionId);
            reaction.setSeverity(SeverityEnum.MODERATE);
            var resp = new TransfusionReactionResponse(UUID.randomUUID(), transfusionId, reactionTypeId,
                    Instant.now(), "Fever", SeverityEnum.MODERATE, "Paracetamol",
                    ReactionOutcomeEnum.RESOLVED, "Nurse A", branchId,
                    LocalDateTime.now(), LocalDateTime.now());

            when(transfusionRepository.findById(transfusionId)).thenReturn(Optional.of(transfusion));
            when(transfusionRepository.save(any(Transfusion.class))).thenReturn(transfusion);
            when(reactionMapper.toEntity(request)).thenReturn(reaction);
            when(reactionRepository.save(any(TransfusionReaction.class))).thenReturn(reaction);
            when(reactionMapper.toResponse(reaction)).thenReturn(resp);

            var result = transfusionService.reportReaction(request);
            assertThat(result.severity()).isEqualTo(SeverityEnum.MODERATE);
            verify(eventPublisher).publishTransfusionReaction(any(TransfusionReactionEvent.class));
        }

        @Test @DisplayName("Should throw when transfusion not found")
        void notFound() {
            var request = new TransfusionReactionCreateRequest(transfusionId, UUID.randomUUID(), Instant.now(),
                    "Fever", SeverityEnum.MILD, null, null, "Nurse A", branchId);
            when(transfusionRepository.findById(transfusionId)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> transfusionService.reportReaction(request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested @DisplayName("getById")
    class GetById {
        @Test @DisplayName("Should return transfusion when found")
        void success() {
            when(transfusionRepository.findById(transfusionId)).thenReturn(Optional.of(transfusion));
            when(transfusionMapper.toResponse(transfusion)).thenReturn(transfusionResponse);
            assertThat(transfusionService.getById(transfusionId).patientName()).isEqualTo("John Doe");
        }

        @Test @DisplayName("Should throw when not found")
        void notFound() {
            when(transfusionRepository.findById(transfusionId)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> transfusionService.getById(transfusionId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested @DisplayName("getByPatient")
    class GetByPatient {
        @Test @DisplayName("Should return paged transfusions for patient")
        void success() {
            Pageable pageable = PageRequest.of(0, 20);
            Page<Transfusion> page = new PageImpl<>(List.of(transfusion), pageable, 1);
            when(transfusionRepository.findByPatientId("PAT-001", pageable)).thenReturn(page);
            when(transfusionMapper.toResponseList(any())).thenReturn(List.of(transfusionResponse));
            var result = transfusionService.getByPatient("PAT-001", pageable);
            assertThat(result.totalElements()).isEqualTo(1);
        }
    }
}
