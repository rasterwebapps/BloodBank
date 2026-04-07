package com.bloodbank.reportingservice.service;

import com.bloodbank.common.exceptions.ResourceNotFoundException;
import com.bloodbank.reportingservice.dto.ReportMetadataCreateRequest;
import com.bloodbank.reportingservice.dto.ReportMetadataResponse;
import com.bloodbank.reportingservice.entity.ReportMetadata;
import com.bloodbank.reportingservice.enums.ReportTypeEnum;
import com.bloodbank.reportingservice.mapper.ReportMetadataMapper;
import com.bloodbank.reportingservice.repository.ReportMetadataRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportService.class);

    private final ReportMetadataRepository reportMetadataRepository;
    private final ReportMetadataMapper reportMetadataMapper;

    public ReportService(ReportMetadataRepository reportMetadataRepository,
                         ReportMetadataMapper reportMetadataMapper) {
        this.reportMetadataRepository = reportMetadataRepository;
        this.reportMetadataMapper = reportMetadataMapper;
    }

    @Transactional
    public ReportMetadataResponse create(ReportMetadataCreateRequest request) {
        log.info("Creating report metadata: {}", request.reportCode());
        ReportMetadata report = reportMetadataMapper.toEntity(request);
        report = reportMetadataRepository.save(report);
        return reportMetadataMapper.toResponse(report);
    }

    public ReportMetadataResponse getById(UUID id) {
        log.debug("Fetching report metadata by id {}", id);
        ReportMetadata report = reportMetadataRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ReportMetadata", "id", id.toString()));
        return reportMetadataMapper.toResponse(report);
    }

    public ReportMetadataResponse getByCode(String reportCode) {
        log.debug("Fetching report metadata by code {}", reportCode);
        ReportMetadata report = reportMetadataRepository.findByReportCode(reportCode)
                .orElseThrow(() -> new ResourceNotFoundException("ReportMetadata", "reportCode", reportCode));
        return reportMetadataMapper.toResponse(report);
    }

    public List<ReportMetadataResponse> getByType(ReportTypeEnum reportType) {
        log.debug("Fetching reports by type {}", reportType);
        List<ReportMetadata> reports = reportMetadataRepository.findByReportTypeAndActiveTrue(reportType);
        return reportMetadataMapper.toResponseList(reports);
    }

    public List<ReportMetadataResponse> getAllActive() {
        log.debug("Fetching all active reports");
        List<ReportMetadata> reports = reportMetadataRepository.findByActiveTrue();
        return reportMetadataMapper.toResponseList(reports);
    }

    @Transactional
    public ReportMetadataResponse update(UUID id, ReportMetadataCreateRequest request) {
        log.info("Updating report metadata {}", id);
        ReportMetadata report = reportMetadataRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ReportMetadata", "id", id.toString()));
        reportMetadataMapper.updateEntity(request, report);
        report = reportMetadataRepository.save(report);
        return reportMetadataMapper.toResponse(report);
    }

    @Transactional
    public void delete(UUID id) {
        log.info("Deleting report metadata {}", id);
        ReportMetadata report = reportMetadataRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ReportMetadata", "id", id.toString()));
        report.setActive(false);
        reportMetadataRepository.save(report);
    }
}
