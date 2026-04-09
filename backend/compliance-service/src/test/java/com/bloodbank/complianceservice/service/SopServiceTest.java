package com.bloodbank.complianceservice.service;

import com.bloodbank.common.exceptions.BusinessException;
import com.bloodbank.common.exceptions.ResourceNotFoundException;
import com.bloodbank.complianceservice.dto.SopCreateRequest;
import com.bloodbank.complianceservice.dto.SopResponse;
import com.bloodbank.complianceservice.entity.SopDocument;
import com.bloodbank.complianceservice.enums.SopCategoryEnum;
import com.bloodbank.complianceservice.enums.SopStatusEnum;
import com.bloodbank.complianceservice.mapper.SopDocumentMapper;
import com.bloodbank.complianceservice.repository.SopDocumentRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
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
class SopServiceTest {

    @Mock
    private SopDocumentRepository sopRepository;

    @Mock
    private SopDocumentMapper sopMapper;

    @InjectMocks
    private SopService sopService;

    private UUID sopId;
    private UUID branchId;
    private UUID frameworkId;
    private SopDocument sopDocument;
    private SopResponse sopResponse;
    private SopCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        sopId = UUID.randomUUID();
        branchId = UUID.randomUUID();
        frameworkId = UUID.randomUUID();

        sopDocument = new SopDocument();
        sopDocument.setId(sopId);
        sopDocument.setSopCode("SOP-001");
        sopDocument.setSopTitle("Blood Collection SOP");
        sopDocument.setCategory(SopCategoryEnum.COLLECTION);
        sopDocument.setFrameworkId(frameworkId);
        sopDocument.setVersionNumber("1.0");
        sopDocument.setEffectiveDate(LocalDate.of(2024, 1, 1));
        sopDocument.setStatus(SopStatusEnum.DRAFT);
        sopDocument.setBranchId(branchId);

        sopResponse = new SopResponse(
                sopId, "SOP-001", "Blood Collection SOP",
                SopCategoryEnum.COLLECTION, frameworkId, "1.0",
                LocalDate.of(2024, 1, 1), null, null, null,
                null, SopStatusEnum.DRAFT, branchId,
                LocalDateTime.now(), LocalDateTime.now()
        );

        createRequest = new SopCreateRequest(
                "SOP-001", "Blood Collection SOP",
                SopCategoryEnum.COLLECTION, frameworkId,
                "1.0", LocalDate.of(2024, 1, 1), null, null, branchId
        );
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("should create SOP document successfully")
        void shouldCreateSopSuccessfully() {
            when(sopMapper.toEntity(createRequest)).thenReturn(sopDocument);
            when(sopRepository.save(any(SopDocument.class))).thenReturn(sopDocument);
            when(sopMapper.toResponse(sopDocument)).thenReturn(sopResponse);

            SopResponse result = sopService.create(createRequest);

            assertThat(result).isNotNull();
            assertThat(result.sopCode()).isEqualTo("SOP-001");
            assertThat(result.sopTitle()).isEqualTo("Blood Collection SOP");
            verify(sopRepository).save(any(SopDocument.class));
        }

        @Test
        @DisplayName("should set status to DRAFT when creating")
        void shouldSetDraftStatus() {
            SopDocument savedSop = new SopDocument();
            savedSop.setStatus(SopStatusEnum.DRAFT);

            when(sopMapper.toEntity(createRequest)).thenReturn(savedSop);
            when(sopRepository.save(any(SopDocument.class))).thenReturn(savedSop);
            when(sopMapper.toResponse(savedSop)).thenReturn(sopResponse);

            sopService.create(createRequest);

            assertThat(savedSop.getStatus()).isEqualTo(SopStatusEnum.DRAFT);
        }
    }

    @Nested
    @DisplayName("getById")
    class GetById {

        @Test
        @DisplayName("should return SOP when found")
        void shouldReturnSopWhenFound() {
            when(sopRepository.findById(sopId)).thenReturn(Optional.of(sopDocument));
            when(sopMapper.toResponse(sopDocument)).thenReturn(sopResponse);

            SopResponse result = sopService.getById(sopId);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(sopId);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            when(sopRepository.findById(sopId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sopService.getById(sopId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getByFrameworkId")
    class GetByFrameworkId {

        @Test
        @DisplayName("should return SOPs by framework ID")
        void shouldReturnByFrameworkId() {
            List<SopDocument> sops = List.of(sopDocument);
            List<SopResponse> responses = List.of(sopResponse);
            when(sopRepository.findByFrameworkId(frameworkId)).thenReturn(sops);
            when(sopMapper.toResponseList(sops)).thenReturn(responses);

            List<SopResponse> result = sopService.getByFrameworkId(frameworkId);

            assertThat(result).hasSize(1);
            verify(sopRepository).findByFrameworkId(frameworkId);
        }
    }

    @Nested
    @DisplayName("getByStatus")
    class GetByStatus {

        @Test
        @DisplayName("should return SOPs by status")
        void shouldReturnByStatus() {
            List<SopDocument> sops = List.of(sopDocument);
            List<SopResponse> responses = List.of(sopResponse);
            when(sopRepository.findByStatus(SopStatusEnum.DRAFT)).thenReturn(sops);
            when(sopMapper.toResponseList(sops)).thenReturn(responses);

            List<SopResponse> result = sopService.getByStatus(SopStatusEnum.DRAFT);

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getByCategory")
    class GetByCategory {

        @Test
        @DisplayName("should return SOPs by category")
        void shouldReturnByCategory() {
            List<SopDocument> sops = List.of(sopDocument);
            List<SopResponse> responses = List.of(sopResponse);
            when(sopRepository.findByCategory(SopCategoryEnum.COLLECTION)).thenReturn(sops);
            when(sopMapper.toResponseList(sops)).thenReturn(responses);

            List<SopResponse> result = sopService.getByCategory(SopCategoryEnum.COLLECTION);

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("updateStatus")
    class UpdateStatus {

        @Test
        @DisplayName("should update DRAFT to REVIEW")
        void shouldUpdateDraftToReview() {
            sopDocument.setStatus(SopStatusEnum.DRAFT);
            SopResponse reviewResponse = new SopResponse(
                    sopId, "SOP-001", "Blood Collection SOP",
                    SopCategoryEnum.COLLECTION, frameworkId, "1.0",
                    LocalDate.of(2024, 1, 1), null, null, null,
                    null, SopStatusEnum.REVIEW, branchId,
                    LocalDateTime.now(), LocalDateTime.now()
            );

            when(sopRepository.findById(sopId)).thenReturn(Optional.of(sopDocument));
            when(sopRepository.save(any(SopDocument.class))).thenReturn(sopDocument);
            when(sopMapper.toResponse(sopDocument)).thenReturn(reviewResponse);

            SopResponse result = sopService.updateStatus(sopId, SopStatusEnum.REVIEW);

            assertThat(result.status()).isEqualTo(SopStatusEnum.REVIEW);
            verify(sopRepository).save(any(SopDocument.class));
        }

        @Test
        @DisplayName("should throw BusinessException for invalid transition DRAFT to APPROVED")
        void shouldThrowForInvalidTransition() {
            sopDocument.setStatus(SopStatusEnum.DRAFT);
            when(sopRepository.findById(sopId)).thenReturn(Optional.of(sopDocument));

            assertThatThrownBy(() -> sopService.updateStatus(sopId, SopStatusEnum.APPROVED))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("should throw BusinessException when retiring from DRAFT")
        void shouldThrowWhenRetiringFromDraft() {
            sopDocument.setStatus(SopStatusEnum.DRAFT);
            when(sopRepository.findById(sopId)).thenReturn(Optional.of(sopDocument));

            assertThatThrownBy(() -> sopService.updateStatus(sopId, SopStatusEnum.RETIRED))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when SOP not found")
        void shouldThrowWhenNotFound() {
            when(sopRepository.findById(sopId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sopService.updateStatus(sopId, SopStatusEnum.REVIEW))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("approve")
    class Approve {

        @Test
        @DisplayName("should approve SOP in REVIEW status")
        void shouldApproveSopInReviewStatus() {
            sopDocument.setStatus(SopStatusEnum.REVIEW);
            SopResponse approvedResponse = new SopResponse(
                    sopId, "SOP-001", "Blood Collection SOP",
                    SopCategoryEnum.COLLECTION, frameworkId, "1.0",
                    LocalDate.of(2024, 1, 1), null, "admin",
                    Instant.now(), null, SopStatusEnum.APPROVED, branchId,
                    LocalDateTime.now(), LocalDateTime.now()
            );

            when(sopRepository.findById(sopId)).thenReturn(Optional.of(sopDocument));
            when(sopRepository.save(any(SopDocument.class))).thenReturn(sopDocument);
            when(sopMapper.toResponse(sopDocument)).thenReturn(approvedResponse);

            SopResponse result = sopService.approve(sopId, "admin");

            assertThat(result.status()).isEqualTo(SopStatusEnum.APPROVED);
            assertThat(sopDocument.getApprovedBy()).isEqualTo("admin");
            assertThat(sopDocument.getApprovedAt()).isNotNull();
        }

        @Test
        @DisplayName("should throw BusinessException when SOP not in REVIEW status")
        void shouldThrowWhenNotInReview() {
            sopDocument.setStatus(SopStatusEnum.DRAFT);
            when(sopRepository.findById(sopId)).thenReturn(Optional.of(sopDocument));

            assertThatThrownBy(() -> sopService.approve(sopId, "admin"))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when SOP not found")
        void shouldThrowWhenNotFound() {
            when(sopRepository.findById(sopId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sopService.approve(sopId, "admin"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("retire")
    class Retire {

        @Test
        @DisplayName("should retire SOP in APPROVED status")
        void shouldRetireApprovedSop() {
            sopDocument.setStatus(SopStatusEnum.APPROVED);
            SopResponse retiredResponse = new SopResponse(
                    sopId, "SOP-001", "Blood Collection SOP",
                    SopCategoryEnum.COLLECTION, frameworkId, "1.0",
                    LocalDate.of(2024, 1, 1), null, null, null,
                    null, SopStatusEnum.RETIRED, branchId,
                    LocalDateTime.now(), LocalDateTime.now()
            );

            when(sopRepository.findById(sopId)).thenReturn(Optional.of(sopDocument));
            when(sopRepository.save(any(SopDocument.class))).thenReturn(sopDocument);
            when(sopMapper.toResponse(sopDocument)).thenReturn(retiredResponse);

            SopResponse result = sopService.retire(sopId);

            assertThat(result.status()).isEqualTo(SopStatusEnum.RETIRED);
        }

        @Test
        @DisplayName("should retire SOP in SUPERSEDED status")
        void shouldRetireSupersededSop() {
            sopDocument.setStatus(SopStatusEnum.SUPERSEDED);

            when(sopRepository.findById(sopId)).thenReturn(Optional.of(sopDocument));
            when(sopRepository.save(any(SopDocument.class))).thenReturn(sopDocument);
            when(sopMapper.toResponse(sopDocument)).thenReturn(sopResponse);

            sopService.retire(sopId);

            assertThat(sopDocument.getStatus()).isEqualTo(SopStatusEnum.RETIRED);
        }

        @Test
        @DisplayName("should throw BusinessException when SOP in DRAFT status")
        void shouldThrowWhenInDraft() {
            sopDocument.setStatus(SopStatusEnum.DRAFT);
            when(sopRepository.findById(sopId)).thenReturn(Optional.of(sopDocument));

            assertThatThrownBy(() -> sopService.retire(sopId))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when SOP not found")
        void shouldThrowWhenNotFound() {
            when(sopRepository.findById(sopId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sopService.retire(sopId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
