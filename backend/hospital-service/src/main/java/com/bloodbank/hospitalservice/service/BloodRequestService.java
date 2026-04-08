package com.bloodbank.hospitalservice.service;

import com.bloodbank.common.dto.PagedResponse;
import com.bloodbank.common.events.BloodRequestCreatedEvent;
import com.bloodbank.common.exceptions.ResourceNotFoundException;
import com.bloodbank.hospitalservice.dto.HospitalRequestCreateRequest;
import com.bloodbank.hospitalservice.dto.HospitalRequestResponse;
import com.bloodbank.hospitalservice.entity.HospitalRequest;
import com.bloodbank.hospitalservice.enums.HospitalRequestStatusEnum;
import com.bloodbank.hospitalservice.event.HospitalEventPublisher;
import com.bloodbank.hospitalservice.mapper.HospitalRequestMapper;
import com.bloodbank.hospitalservice.repository.HospitalRepository;
import com.bloodbank.hospitalservice.repository.HospitalRequestRepository;

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
public class BloodRequestService {

    private static final Logger log = LoggerFactory.getLogger(BloodRequestService.class);

    private final HospitalRequestRepository requestRepository;
    private final HospitalRepository hospitalRepository;
    private final HospitalRequestMapper requestMapper;
    private final HospitalEventPublisher eventPublisher;

    public BloodRequestService(HospitalRequestRepository requestRepository,
                               HospitalRepository hospitalRepository,
                               HospitalRequestMapper requestMapper,
                               HospitalEventPublisher eventPublisher) {
        this.requestRepository = requestRepository;
        this.hospitalRepository = hospitalRepository;
        this.requestMapper = requestMapper;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public HospitalRequestResponse createRequest(HospitalRequestCreateRequest request) {
        log.info("Creating blood request for hospital: {}", request.hospitalId());

        hospitalRepository.findById(request.hospitalId())
                .orElseThrow(() -> new ResourceNotFoundException("Hospital", "id", request.hospitalId()));

        HospitalRequest hospitalRequest = requestMapper.toEntity(request);
        hospitalRequest.setRequestNumber("REQ-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        hospitalRequest.setStatus(HospitalRequestStatusEnum.PENDING);
        hospitalRequest.setUnitsFulfilled(0);
        hospitalRequest.setBranchId(request.branchId());

        hospitalRequest = requestRepository.save(hospitalRequest);
        log.info("Blood request created: {}", hospitalRequest.getRequestNumber());

        eventPublisher.publishBloodRequestCreated(new BloodRequestCreatedEvent(
                hospitalRequest.getId(),
                hospitalRequest.getHospitalId(),
                hospitalRequest.getBranchId(),
                hospitalRequest.getPatientBloodGroupId().toString(),
                hospitalRequest.getUnitsRequested(),
                Instant.now()
        ));

        return requestMapper.toResponse(hospitalRequest);
    }

    public HospitalRequestResponse getRequestById(UUID id) {
        log.debug("Fetching request by id: {}", id);
        HospitalRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("HospitalRequest", "id", id));
        return requestMapper.toResponse(request);
    }

    public HospitalRequestResponse getRequestByNumber(String requestNumber) {
        log.debug("Fetching request by number: {}", requestNumber);
        HospitalRequest request = requestRepository.findByRequestNumber(requestNumber)
                .orElseThrow(() -> new ResourceNotFoundException("HospitalRequest", "requestNumber", requestNumber));
        return requestMapper.toResponse(request);
    }

    public PagedResponse<HospitalRequestResponse> getRequestsByHospitalId(UUID hospitalId, Pageable pageable) {
        log.debug("Fetching requests for hospital: {}", hospitalId);
        Page<HospitalRequest> page = requestRepository.findByHospitalId(hospitalId, pageable);
        return toPagedResponse(page);
    }

    public PagedResponse<HospitalRequestResponse> getRequestsByStatus(HospitalRequestStatusEnum status,
                                                                       Pageable pageable) {
        log.debug("Fetching requests by status: {}", status);
        Page<HospitalRequest> page = requestRepository.findByStatus(status, pageable);
        return toPagedResponse(page);
    }

    public PagedResponse<HospitalRequestResponse> getAllRequests(Pageable pageable) {
        log.debug("Fetching all requests");
        Page<HospitalRequest> page = requestRepository.findAll(pageable);
        return toPagedResponse(page);
    }

    @Transactional
    public HospitalRequestResponse updateRequestStatus(UUID id, HospitalRequestStatusEnum status,
                                                        String rejectionReason) {
        log.info("Updating request {} status to: {}", id, status);
        HospitalRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("HospitalRequest", "id", id));
        request.setStatus(status);
        if (rejectionReason != null) {
            request.setRejectionReason(rejectionReason);
        }
        request = requestRepository.save(request);
        return requestMapper.toResponse(request);
    }

    @Transactional
    public HospitalRequestResponse cancelRequest(UUID id) {
        log.info("Cancelling request: {}", id);
        HospitalRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("HospitalRequest", "id", id));
        request.setStatus(HospitalRequestStatusEnum.CANCELLED);
        request = requestRepository.save(request);
        return requestMapper.toResponse(request);
    }

    private PagedResponse<HospitalRequestResponse> toPagedResponse(Page<HospitalRequest> page) {
        List<HospitalRequestResponse> content = requestMapper.toResponseList(page.getContent());
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
