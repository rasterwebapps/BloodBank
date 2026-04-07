package com.bloodbank.transfusionservice.service;

import com.bloodbank.common.exceptions.ResourceNotFoundException;
import com.bloodbank.transfusionservice.dto.HemovigilanceReportCreateRequest;
import com.bloodbank.transfusionservice.dto.HemovigilanceReportResponse;
import com.bloodbank.transfusionservice.dto.LookBackInvestigationCreateRequest;
import com.bloodbank.transfusionservice.dto.LookBackInvestigationResponse;
import com.bloodbank.transfusionservice.entity.HemovigilanceReport;
import com.bloodbank.transfusionservice.entity.LookBackInvestigation;
import com.bloodbank.transfusionservice.enums.HemovigilanceStatusEnum;
import com.bloodbank.transfusionservice.enums.LookBackStatusEnum;
import com.bloodbank.transfusionservice.mapper.HemovigilanceReportMapper;
import com.bloodbank.transfusionservice.mapper.LookBackInvestigationMapper;
import com.bloodbank.transfusionservice.repository.HemovigilanceReportRepository;
import com.bloodbank.transfusionservice.repository.LookBackInvestigationRepository;
import com.bloodbank.transfusionservice.repository.TransfusionReactionRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class HemovigilanceService {

    private static final Logger log = LoggerFactory.getLogger(HemovigilanceService.class);

    private final HemovigilanceReportRepository reportRepository;
    private final LookBackInvestigationRepository investigationRepository;
    private final TransfusionReactionRepository reactionRepository;
    private final HemovigilanceReportMapper reportMapper;
    private final LookBackInvestigationMapper investigationMapper;

    public HemovigilanceService(HemovigilanceReportRepository reportRepository,
                                LookBackInvestigationRepository investigationRepository,
                                TransfusionReactionRepository reactionRepository,
                                HemovigilanceReportMapper reportMapper,
                                LookBackInvestigationMapper investigationMapper) {
        this.reportRepository = reportRepository;
        this.investigationRepository = investigationRepository;
        this.reactionRepository = reactionRepository;
        this.reportMapper = reportMapper;
        this.investigationMapper = investigationMapper;
    }

    @Transactional
    public HemovigilanceReportResponse createReport(HemovigilanceReportCreateRequest request) {
        log.info("Creating hemovigilance report for reaction: {}", request.transfusionReactionId());

        reactionRepository.findById(request.transfusionReactionId())
                .orElseThrow(() -> new ResourceNotFoundException("TransfusionReaction", "id", request.transfusionReactionId()));

        HemovigilanceReport entity = reportMapper.toEntity(request);
        entity.setReportNumber("HV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        entity.setReportDate(Instant.now());
        entity.setStatus(HemovigilanceStatusEnum.OPEN);
        entity.setBranchId(request.branchId());

        entity = reportRepository.save(entity);
        log.info("Hemovigilance report created with number: {}", entity.getReportNumber());
        return reportMapper.toResponse(entity);
    }

    @Transactional
    public HemovigilanceReportResponse updateReportStatus(UUID id, String status) {
        log.info("Updating hemovigilance report status: {} to {}", id, status);

        HemovigilanceReport report = reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("HemovigilanceReport", "id", id));

        report.setStatus(HemovigilanceStatusEnum.valueOf(status));
        report = reportRepository.save(report);
        return reportMapper.toResponse(report);
    }

    @Transactional
    public LookBackInvestigationResponse createLookBackInvestigation(LookBackInvestigationCreateRequest request) {
        log.info("Creating lookback investigation for donor: {}", request.donorId());

        LookBackInvestigation entity = investigationMapper.toEntity(request);
        entity.setInvestigationNumber("LB-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        entity.setInvestigationDate(Instant.now());
        entity.setStatus(LookBackStatusEnum.INITIATED);
        entity.setBranchId(request.branchId());

        entity = investigationRepository.save(entity);
        log.info("Lookback investigation created with number: {}", entity.getInvestigationNumber());
        return investigationMapper.toResponse(entity);
    }

    @Transactional
    public LookBackInvestigationResponse updateLookBackStatus(UUID id, String status, String findings) {
        log.info("Updating lookback investigation status: {} to {}", id, status);

        LookBackInvestigation investigation = investigationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LookBackInvestigation", "id", id));

        investigation.setStatus(LookBackStatusEnum.valueOf(status));
        if (findings != null) {
            investigation.setFindings(findings);
        }
        investigation = investigationRepository.save(investigation);
        return investigationMapper.toResponse(investigation);
    }

    public HemovigilanceReportResponse getReportById(UUID id) {
        log.debug("Fetching hemovigilance report by id: {}", id);
        HemovigilanceReport entity = reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("HemovigilanceReport", "id", id));
        return reportMapper.toResponse(entity);
    }

    public LookBackInvestigationResponse getInvestigationById(UUID id) {
        log.debug("Fetching lookback investigation by id: {}", id);
        LookBackInvestigation entity = investigationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LookBackInvestigation", "id", id));
        return investigationMapper.toResponse(entity);
    }
}
