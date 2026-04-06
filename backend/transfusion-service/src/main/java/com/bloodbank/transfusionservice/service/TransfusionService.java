package com.bloodbank.transfusionservice.service;

import com.bloodbank.common.dto.PagedResponse;
import com.bloodbank.common.events.TransfusionCompletedEvent;
import com.bloodbank.common.events.TransfusionReactionEvent;
import com.bloodbank.common.exceptions.BusinessException;
import com.bloodbank.common.exceptions.ResourceNotFoundException;
import com.bloodbank.common.model.enums.TransfusionStatusEnum;
import com.bloodbank.transfusionservice.dto.TransfusionCompleteRequest;
import com.bloodbank.transfusionservice.dto.TransfusionCreateRequest;
import com.bloodbank.transfusionservice.dto.TransfusionReactionCreateRequest;
import com.bloodbank.transfusionservice.dto.TransfusionReactionResponse;
import com.bloodbank.transfusionservice.dto.TransfusionResponse;
import com.bloodbank.transfusionservice.entity.BloodIssue;
import com.bloodbank.transfusionservice.entity.Transfusion;
import com.bloodbank.transfusionservice.entity.TransfusionReaction;
import com.bloodbank.transfusionservice.enums.IssueStatusEnum;
import com.bloodbank.transfusionservice.enums.TransfusionOutcomeEnum;
import com.bloodbank.transfusionservice.event.TransfusionEventPublisher;
import com.bloodbank.transfusionservice.mapper.TransfusionMapper;
import com.bloodbank.transfusionservice.mapper.TransfusionReactionMapper;
import com.bloodbank.transfusionservice.repository.BloodIssueRepository;
import com.bloodbank.transfusionservice.repository.TransfusionReactionRepository;
import com.bloodbank.transfusionservice.repository.TransfusionRepository;

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
public class TransfusionService {

    private static final Logger log = LoggerFactory.getLogger(TransfusionService.class);

    private final TransfusionRepository transfusionRepository;
    private final TransfusionReactionRepository reactionRepository;
    private final BloodIssueRepository bloodIssueRepository;
    private final TransfusionMapper transfusionMapper;
    private final TransfusionReactionMapper reactionMapper;
    private final TransfusionEventPublisher eventPublisher;

    public TransfusionService(TransfusionRepository transfusionRepository,
                              TransfusionReactionRepository reactionRepository,
                              BloodIssueRepository bloodIssueRepository,
                              TransfusionMapper transfusionMapper,
                              TransfusionReactionMapper reactionMapper,
                              TransfusionEventPublisher eventPublisher) {
        this.transfusionRepository = transfusionRepository;
        this.reactionRepository = reactionRepository;
        this.bloodIssueRepository = bloodIssueRepository;
        this.transfusionMapper = transfusionMapper;
        this.reactionMapper = reactionMapper;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public TransfusionResponse startTransfusion(TransfusionCreateRequest request) {
        log.info("Starting transfusion for patient: {}", request.patientName());

        BloodIssue bloodIssue = bloodIssueRepository.findById(request.bloodIssueId())
                .orElseThrow(() -> new ResourceNotFoundException("BloodIssue", "id", request.bloodIssueId()));

        if (bloodIssue.getStatus() != IssueStatusEnum.ISSUED) {
            throw new BusinessException("Blood issue must be in ISSUED status to start transfusion", "INVALID_STATUS");
        }

        Transfusion entity = transfusionMapper.toEntity(request);
        entity.setTransfusionStart(Instant.now());
        entity.setStatus(TransfusionStatusEnum.IN_PROGRESS);
        entity.setBranchId(request.branchId());

        entity = transfusionRepository.save(entity);
        log.info("Transfusion started for patient: {}", request.patientName());
        return transfusionMapper.toResponse(entity);
    }

    @Transactional
    public TransfusionResponse completeTransfusion(UUID id, TransfusionCompleteRequest request) {
        log.info("Completing transfusion: {}", id);

        Transfusion transfusion = transfusionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transfusion", "id", id));

        if (transfusion.getStatus() != TransfusionStatusEnum.IN_PROGRESS) {
            throw new BusinessException("Transfusion must be IN_PROGRESS to complete", "INVALID_STATUS");
        }

        transfusion.setTransfusionEnd(Instant.now());
        transfusion.setVolumeTransfusedMl(request.volumeTransfusedMl());
        transfusion.setPostVitalSigns(request.postVitalSigns());
        transfusion.setStatus(TransfusionStatusEnum.COMPLETED);
        transfusion.setOutcome(TransfusionOutcomeEnum.SUCCESSFUL);
        if (request.notes() != null) {
            transfusion.setNotes(request.notes());
        }

        transfusion = transfusionRepository.save(transfusion);

        BloodIssue bloodIssue = bloodIssueRepository.findById(transfusion.getBloodIssueId())
                .orElse(null);
        if (bloodIssue != null) {
            bloodIssue.setStatus(IssueStatusEnum.TRANSFUSED);
            bloodIssueRepository.save(bloodIssue);
        }

        eventPublisher.publishTransfusionCompleted(new TransfusionCompletedEvent(
                transfusion.getId(),
                transfusion.getBloodIssueId(),
                transfusion.getBranchId(),
                Instant.now()
        ));

        log.info("Transfusion completed: {}", id);
        return transfusionMapper.toResponse(transfusion);
    }

    @Transactional
    public TransfusionReactionResponse reportReaction(TransfusionReactionCreateRequest request) {
        log.info("Reporting transfusion reaction for transfusion: {}", request.transfusionId());

        Transfusion transfusion = transfusionRepository.findById(request.transfusionId())
                .orElseThrow(() -> new ResourceNotFoundException("Transfusion", "id", request.transfusionId()));

        transfusion.setStatus(TransfusionStatusEnum.REACTION_REPORTED);
        transfusion.setOutcome(TransfusionOutcomeEnum.REACTION);
        transfusionRepository.save(transfusion);

        TransfusionReaction entity = reactionMapper.toEntity(request);
        entity.setBranchId(request.branchId());
        entity = reactionRepository.save(entity);

        eventPublisher.publishTransfusionReaction(new TransfusionReactionEvent(
                transfusion.getId(),
                transfusion.getBloodIssueId(),
                transfusion.getBranchId(),
                request.severity().name(),
                Instant.now()
        ));

        log.info("Transfusion reaction reported for transfusion: {}", request.transfusionId());
        return reactionMapper.toResponse(entity);
    }

    public TransfusionResponse getById(UUID id) {
        log.debug("Fetching transfusion by id: {}", id);
        Transfusion entity = transfusionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transfusion", "id", id));
        return transfusionMapper.toResponse(entity);
    }

    public PagedResponse<TransfusionResponse> getByPatient(String patientId, Pageable pageable) {
        log.debug("Fetching transfusions for patient: {}", patientId);
        Page<Transfusion> page = transfusionRepository.findByPatientId(patientId, pageable);
        return toPagedResponse(page);
    }

    private PagedResponse<TransfusionResponse> toPagedResponse(Page<Transfusion> page) {
        List<TransfusionResponse> content = transfusionMapper.toResponseList(page.getContent());
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
