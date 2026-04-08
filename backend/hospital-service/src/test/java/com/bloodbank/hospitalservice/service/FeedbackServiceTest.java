package com.bloodbank.hospitalservice.service;

import com.bloodbank.common.dto.PagedResponse;
import com.bloodbank.common.exceptions.ResourceNotFoundException;
import com.bloodbank.hospitalservice.dto.HospitalFeedbackCreateRequest;
import com.bloodbank.hospitalservice.dto.HospitalFeedbackResponse;
import com.bloodbank.hospitalservice.entity.Hospital;
import com.bloodbank.hospitalservice.entity.HospitalFeedback;
import com.bloodbank.hospitalservice.enums.FeedbackCategoryEnum;
import com.bloodbank.hospitalservice.mapper.HospitalFeedbackMapper;
import com.bloodbank.hospitalservice.repository.HospitalFeedbackRepository;
import com.bloodbank.hospitalservice.repository.HospitalRepository;
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
class FeedbackServiceTest {

    @Mock
    private HospitalFeedbackRepository feedbackRepository;

    @Mock
    private HospitalRepository hospitalRepository;

    @Mock
    private HospitalFeedbackMapper feedbackMapper;

    @InjectMocks
    private FeedbackService feedbackService;

    private UUID feedbackId;
    private UUID hospitalId;
    private UUID requestId;
    private UUID branchId;
    private HospitalFeedback feedback;
    private HospitalFeedbackResponse feedbackResponse;
    private HospitalFeedbackCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        feedbackId = UUID.randomUUID();
        hospitalId = UUID.randomUUID();
        requestId = UUID.randomUUID();
        branchId = UUID.randomUUID();

        feedback = new HospitalFeedback();
        feedback.setHospitalId(hospitalId);
        feedback.setRequestId(requestId);
        feedback.setRating(4);
        feedback.setCategory(FeedbackCategoryEnum.SERVICE_QUALITY);
        feedback.setComments("Good service overall");
        feedback.setFeedbackDate(Instant.now());

        feedbackResponse = new HospitalFeedbackResponse(
                feedbackId, hospitalId, requestId, Instant.now(),
                4, FeedbackCategoryEnum.SERVICE_QUALITY,
                "Good service overall", null, null, null,
                branchId, LocalDateTime.now(), LocalDateTime.now()
        );

        createRequest = new HospitalFeedbackCreateRequest(
                hospitalId, requestId, 4,
                FeedbackCategoryEnum.SERVICE_QUALITY,
                "Good service overall", branchId
        );
    }

    @Nested
    @DisplayName("submitFeedback")
    class SubmitFeedback {

        @Test
        @DisplayName("Should submit feedback successfully")
        void shouldSubmitFeedbackSuccessfully() {
            when(hospitalRepository.findById(hospitalId)).thenReturn(Optional.of(new Hospital()));
            when(feedbackMapper.toEntity(createRequest)).thenReturn(feedback);
            when(feedbackRepository.save(any(HospitalFeedback.class))).thenReturn(feedback);
            when(feedbackMapper.toResponse(feedback)).thenReturn(feedbackResponse);

            HospitalFeedbackResponse result = feedbackService.submitFeedback(createRequest);

            assertThat(result).isNotNull();
            assertThat(result.rating()).isEqualTo(4);
            assertThat(result.category()).isEqualTo(FeedbackCategoryEnum.SERVICE_QUALITY);
            verify(feedbackRepository).save(any(HospitalFeedback.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when hospital not found")
        void shouldThrowWhenHospitalNotFound() {
            when(hospitalRepository.findById(hospitalId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> feedbackService.submitFeedback(createRequest))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(feedbackRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should set feedbackDate and branchId on creation")
        void shouldSetFeedbackDateAndBranchId() {
            HospitalFeedback capturedFeedback = new HospitalFeedback();
            when(hospitalRepository.findById(hospitalId)).thenReturn(Optional.of(new Hospital()));
            when(feedbackMapper.toEntity(createRequest)).thenReturn(capturedFeedback);
            when(feedbackRepository.save(any(HospitalFeedback.class))).thenAnswer(inv -> inv.getArgument(0));
            when(feedbackMapper.toResponse(any(HospitalFeedback.class))).thenReturn(feedbackResponse);

            feedbackService.submitFeedback(createRequest);

            assertThat(capturedFeedback.getFeedbackDate()).isNotNull();
            assertThat(capturedFeedback.getBranchId()).isEqualTo(branchId);
        }
    }

    @Nested
    @DisplayName("getFeedbackById")
    class GetFeedbackById {

        @Test
        @DisplayName("Should return feedback when found")
        void shouldReturnFeedbackWhenFound() {
            when(feedbackRepository.findById(feedbackId)).thenReturn(Optional.of(feedback));
            when(feedbackMapper.toResponse(feedback)).thenReturn(feedbackResponse);

            HospitalFeedbackResponse result = feedbackService.getFeedbackById(feedbackId);

            assertThat(result).isNotNull();
            assertThat(result.rating()).isEqualTo(4);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            when(feedbackRepository.findById(feedbackId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> feedbackService.getFeedbackById(feedbackId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getFeedbackByHospitalId")
    class GetFeedbackByHospitalId {

        @Test
        @DisplayName("Should return paged feedback for hospital")
        void shouldReturnPagedFeedback() {
            Pageable pageable = PageRequest.of(0, 20);
            Page<HospitalFeedback> page = new PageImpl<>(List.of(feedback), pageable, 1);
            when(feedbackRepository.findByHospitalId(hospitalId, pageable)).thenReturn(page);
            when(feedbackMapper.toResponseList(List.of(feedback))).thenReturn(List.of(feedbackResponse));

            PagedResponse<HospitalFeedbackResponse> result = feedbackService.getFeedbackByHospitalId(hospitalId, pageable);

            assertThat(result.content()).hasSize(1);
            assertThat(result.totalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should return empty paged response when no feedback")
        void shouldReturnEmptyPagedResponse() {
            Pageable pageable = PageRequest.of(0, 20);
            Page<HospitalFeedback> page = new PageImpl<>(List.of(), pageable, 0);
            when(feedbackRepository.findByHospitalId(hospitalId, pageable)).thenReturn(page);
            when(feedbackMapper.toResponseList(List.of())).thenReturn(List.of());

            PagedResponse<HospitalFeedbackResponse> result = feedbackService.getFeedbackByHospitalId(hospitalId, pageable);

            assertThat(result.content()).isEmpty();
            assertThat(result.totalElements()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("getFeedbackByRequestId")
    class GetFeedbackByRequestId {

        @Test
        @DisplayName("Should return feedback for request")
        void shouldReturnFeedbackForRequest() {
            when(feedbackRepository.findByRequestId(requestId)).thenReturn(List.of(feedback));
            when(feedbackMapper.toResponseList(List.of(feedback))).thenReturn(List.of(feedbackResponse));

            List<HospitalFeedbackResponse> result = feedbackService.getFeedbackByRequestId(requestId);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).category()).isEqualTo(FeedbackCategoryEnum.SERVICE_QUALITY);
        }

        @Test
        @DisplayName("Should return empty list when no feedback for request")
        void shouldReturnEmptyListWhenNoFeedback() {
            when(feedbackRepository.findByRequestId(requestId)).thenReturn(List.of());
            when(feedbackMapper.toResponseList(List.of())).thenReturn(List.of());

            List<HospitalFeedbackResponse> result = feedbackService.getFeedbackByRequestId(requestId);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("respondToFeedback")
    class RespondToFeedback {

        @Test
        @DisplayName("Should respond to feedback successfully")
        void shouldRespondToFeedbackSuccessfully() {
            when(feedbackRepository.findById(feedbackId)).thenReturn(Optional.of(feedback));
            when(feedbackRepository.save(any(HospitalFeedback.class))).thenReturn(feedback);
            when(feedbackMapper.toResponse(feedback)).thenReturn(feedbackResponse);

            HospitalFeedbackResponse result = feedbackService.respondToFeedback(
                    feedbackId, "Thank you for your feedback", "admin@bloodbank.com");

            assertThat(result).isNotNull();
            verify(feedbackRepository).save(feedback);
        }

        @Test
        @DisplayName("Should set response fields on entity")
        void shouldSetResponseFields() {
            when(feedbackRepository.findById(feedbackId)).thenReturn(Optional.of(feedback));
            when(feedbackRepository.save(any(HospitalFeedback.class))).thenReturn(feedback);
            when(feedbackMapper.toResponse(feedback)).thenReturn(feedbackResponse);

            feedbackService.respondToFeedback(
                    feedbackId, "Thank you for your feedback", "admin@bloodbank.com");

            assertThat(feedback.getResponse()).isEqualTo("Thank you for your feedback");
            assertThat(feedback.getRespondedBy()).isEqualTo("admin@bloodbank.com");
            assertThat(feedback.getRespondedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when feedback not found")
        void shouldThrowWhenFeedbackNotFound() {
            when(feedbackRepository.findById(feedbackId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> feedbackService.respondToFeedback(
                    feedbackId, "Thank you", "admin@bloodbank.com"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
