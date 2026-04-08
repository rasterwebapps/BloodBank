package com.bloodbank.hospitalservice.service;

import com.bloodbank.common.dto.PagedResponse;
import com.bloodbank.common.exceptions.ResourceNotFoundException;
import com.bloodbank.hospitalservice.dto.HospitalFeedbackCreateRequest;
import com.bloodbank.hospitalservice.dto.HospitalFeedbackResponse;
import com.bloodbank.hospitalservice.entity.HospitalFeedback;
import com.bloodbank.hospitalservice.mapper.HospitalFeedbackMapper;
import com.bloodbank.hospitalservice.repository.HospitalFeedbackRepository;
import com.bloodbank.hospitalservice.repository.HospitalRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class FeedbackService {

    private static final Logger log = LoggerFactory.getLogger(FeedbackService.class);

    private final HospitalFeedbackRepository feedbackRepository;
    private final HospitalRepository hospitalRepository;
    private final HospitalFeedbackMapper feedbackMapper;

    public FeedbackService(HospitalFeedbackRepository feedbackRepository,
                           HospitalRepository hospitalRepository,
                           HospitalFeedbackMapper feedbackMapper) {
        this.feedbackRepository = feedbackRepository;
        this.hospitalRepository = hospitalRepository;
        this.feedbackMapper = feedbackMapper;
    }

    @Transactional
    public HospitalFeedbackResponse submitFeedback(HospitalFeedbackCreateRequest request) {
        log.info("Submitting feedback for hospital: {}", request.hospitalId());

        hospitalRepository.findById(request.hospitalId())
                .orElseThrow(() -> new ResourceNotFoundException("Hospital", "id", request.hospitalId()));

        HospitalFeedback feedback = feedbackMapper.toEntity(request);
        feedback.setFeedbackDate(Instant.now());
        feedback.setBranchId(request.branchId());

        feedback = feedbackRepository.save(feedback);
        log.info("Feedback submitted for hospital: {}", request.hospitalId());
        return feedbackMapper.toResponse(feedback);
    }

    public HospitalFeedbackResponse getFeedbackById(UUID id) {
        log.debug("Fetching feedback by id: {}", id);
        HospitalFeedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("HospitalFeedback", "id", id));
        return feedbackMapper.toResponse(feedback);
    }

    public PagedResponse<HospitalFeedbackResponse> getFeedbackByHospitalId(UUID hospitalId, Pageable pageable) {
        log.debug("Fetching feedback for hospital: {}", hospitalId);
        Page<HospitalFeedback> page = feedbackRepository.findByHospitalId(hospitalId, pageable);
        return toPagedResponse(page);
    }

    public List<HospitalFeedbackResponse> getFeedbackByRequestId(UUID requestId) {
        log.debug("Fetching feedback for request: {}", requestId);
        List<HospitalFeedback> feedbackList = feedbackRepository.findByRequestId(requestId);
        return feedbackMapper.toResponseList(feedbackList);
    }

    @Transactional
    public HospitalFeedbackResponse respondToFeedback(UUID id, String responseText, String respondedBy) {
        log.info("Responding to feedback: {}", id);
        HospitalFeedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("HospitalFeedback", "id", id));

        feedback.setResponse(responseText);
        feedback.setRespondedBy(respondedBy);
        feedback.setRespondedAt(Instant.now());

        feedback = feedbackRepository.save(feedback);
        return feedbackMapper.toResponse(feedback);
    }

    private PagedResponse<HospitalFeedbackResponse> toPagedResponse(Page<HospitalFeedback> page) {
        List<HospitalFeedbackResponse> content = feedbackMapper.toResponseList(page.getContent());
        return new PagedResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
