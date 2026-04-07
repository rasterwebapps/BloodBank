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
import com.bloodbank.transfusionservice.mapper.CrossMatchRequestMapper;
import com.bloodbank.transfusionservice.mapper.CrossMatchResultMapper;
import com.bloodbank.transfusionservice.repository.CrossMatchRequestRepository;
import com.bloodbank.transfusionservice.repository.CrossMatchResultRepository;

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
public class CrossMatchService {

    private static final Logger log = LoggerFactory.getLogger(CrossMatchService.class);

    private final CrossMatchRequestRepository requestRepository;
    private final CrossMatchResultRepository resultRepository;
    private final CrossMatchRequestMapper requestMapper;
    private final CrossMatchResultMapper resultMapper;

    public CrossMatchService(CrossMatchRequestRepository requestRepository,
                             CrossMatchResultRepository resultRepository,
                             CrossMatchRequestMapper requestMapper,
                             CrossMatchResultMapper resultMapper) {
        this.requestRepository = requestRepository;
        this.resultRepository = resultRepository;
        this.requestMapper = requestMapper;
        this.resultMapper = resultMapper;
    }

    @Transactional
    public CrossMatchRequestResponse createRequest(CrossMatchRequestCreateRequest request) {
        log.info("Creating crossmatch request for patient: {}", request.patientName());

        CrossMatchRequest entity = requestMapper.toEntity(request);
        entity.setRequestNumber("CM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        entity.setStatus(RequestStatusEnum.PENDING);
        entity.setBranchId(request.branchId());

        entity = requestRepository.save(entity);
        log.info("Crossmatch request created with number: {}", entity.getRequestNumber());
        return requestMapper.toResponse(entity);
    }

    @Transactional
    public CrossMatchResultResponse addResult(CrossMatchResultCreateRequest request) {
        log.info("Adding crossmatch result for request: {}", request.crossmatchRequestId());

        CrossMatchRequest crossMatchRequest = requestRepository.findById(request.crossmatchRequestId())
                .orElseThrow(() -> new ResourceNotFoundException("CrossMatchRequest", "id", request.crossmatchRequestId()));

        CrossMatchResult entity = resultMapper.toEntity(request);
        entity.setPerformedAt(Instant.now());
        entity.setBranchId(request.branchId());

        entity = resultRepository.save(entity);

        crossMatchRequest.setStatus(RequestStatusEnum.MATCHED);
        requestRepository.save(crossMatchRequest);

        log.info("Crossmatch result added for request: {}", request.crossmatchRequestId());
        return resultMapper.toResponse(entity);
    }

    public CrossMatchRequestResponse getRequestById(UUID id) {
        log.debug("Fetching crossmatch request by id: {}", id);
        CrossMatchRequest entity = requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CrossMatchRequest", "id", id));
        return requestMapper.toResponse(entity);
    }

    public List<CrossMatchResultResponse> getResultsByRequestId(UUID requestId) {
        log.debug("Fetching crossmatch results for request: {}", requestId);
        requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("CrossMatchRequest", "id", requestId));
        List<CrossMatchResult> results = resultRepository.findByCrossmatchRequestId(requestId);
        return resultMapper.toResponseList(results);
    }

    public PagedResponse<CrossMatchRequestResponse> getRequestsByStatus(RequestStatusEnum status, Pageable pageable) {
        log.debug("Fetching crossmatch requests by status: {}", status);
        Page<CrossMatchRequest> page = requestRepository.findByStatus(status, pageable);
        return toPagedResponse(page);
    }

    private PagedResponse<CrossMatchRequestResponse> toPagedResponse(Page<CrossMatchRequest> page) {
        List<CrossMatchRequestResponse> content = requestMapper.toResponseList(page.getContent());
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
