package com.bloodbank.complianceservice.service;

import com.bloodbank.common.events.RecallInitiatedEvent;
import com.bloodbank.common.exceptions.BusinessException;
import com.bloodbank.common.exceptions.ResourceNotFoundException;
import com.bloodbank.complianceservice.dto.RecallCreateRequest;
import com.bloodbank.complianceservice.dto.RecallResponse;
import com.bloodbank.complianceservice.entity.RecallRecord;
import com.bloodbank.complianceservice.enums.RecallStatusEnum;
import com.bloodbank.complianceservice.enums.RecallTypeEnum;
import com.bloodbank.complianceservice.event.EventPublisher;
import com.bloodbank.complianceservice.mapper.RecallRecordMapper;
import com.bloodbank.complianceservice.repository.RecallRecordRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class RecallService {

    private static final Logger log = LoggerFactory.getLogger(RecallService.class);

    private final RecallRecordRepository recallRepository;
    private final RecallRecordMapper recallMapper;
    private final EventPublisher eventPublisher;

    public RecallService(RecallRecordRepository recallRepository,
                         RecallRecordMapper recallMapper,
                         EventPublisher eventPublisher) {
        this.recallRepository = recallRepository;
        this.recallMapper = recallMapper;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public RecallResponse create(RecallCreateRequest request) {
        log.info("Creating recall: type={}, reason={}", request.recallType(), request.recallReason());
        RecallRecord recall = recallMapper.toEntity(request);
        recall.setBranchId(request.branchId());
        recall.setRecallNumber("RCL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        recall.setInitiatedDate(Instant.now());
        recall.setStatus(RecallStatusEnum.INITIATED);
        recall = recallRepository.save(recall);

        eventPublisher.publishRecallInitiated(new RecallInitiatedEvent(
                recall.getId(),
                recall.getBranchId(),
                recall.getRecallReason(),
                List.of(),
                Instant.now()
        ));

        return recallMapper.toResponse(recall);
    }

    public RecallResponse getById(UUID id) {
        RecallRecord recall = recallRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RecallRecord", "id", id));
        return recallMapper.toResponse(recall);
    }

    public RecallResponse getByRecallNumber(String recallNumber) {
        RecallRecord recall = recallRepository.findByRecallNumber(recallNumber)
                .orElseThrow(() -> new ResourceNotFoundException("RecallRecord", "recallNumber", recallNumber));
        return recallMapper.toResponse(recall);
    }

    public List<RecallResponse> getByStatus(RecallStatusEnum status) {
        return recallMapper.toResponseList(recallRepository.findByStatus(status));
    }

    public List<RecallResponse> getByType(RecallTypeEnum type) {
        return recallMapper.toResponseList(recallRepository.findByRecallType(type));
    }

    @Transactional
    public RecallResponse updateStatus(UUID id, RecallStatusEnum status) {
        RecallRecord recall = recallRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RecallRecord", "id", id));

        if (recall.getStatus() == RecallStatusEnum.CLOSED) {
            throw new BusinessException("Cannot update status of a closed recall", "RECALL_CLOSED");
        }

        recall.setStatus(status);
        recall = recallRepository.save(recall);
        log.info("Updated recall status: id={}, newStatus={}", id, status);
        return recallMapper.toResponse(recall);
    }

    @Transactional
    public RecallResponse close(UUID id, String closedBy) {
        RecallRecord recall = recallRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RecallRecord", "id", id));

        if (recall.getStatus() == RecallStatusEnum.CLOSED) {
            throw new BusinessException("Recall is already closed", "RECALL_ALREADY_CLOSED");
        }

        recall.setStatus(RecallStatusEnum.CLOSED);
        recall.setClosureDate(Instant.now());
        recall.setClosedBy(closedBy);
        recall = recallRepository.save(recall);
        log.info("Closed recall: id={}, closedBy={}", id, closedBy);
        return recallMapper.toResponse(recall);
    }
}
