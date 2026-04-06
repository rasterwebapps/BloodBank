package com.bloodbank.labservice.service;

import com.bloodbank.common.dto.PagedResponse;
import com.bloodbank.common.exceptions.ResourceNotFoundException;
import com.bloodbank.labservice.dto.QualityControlCreateRequest;
import com.bloodbank.labservice.dto.QualityControlResponse;
import com.bloodbank.labservice.entity.QualityControlRecord;
import com.bloodbank.labservice.enums.QcStatusEnum;
import com.bloodbank.labservice.mapper.QualityControlMapper;
import com.bloodbank.labservice.repository.LabInstrumentRepository;
import com.bloodbank.labservice.repository.QualityControlRecordRepository;

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
public class QualityControlService {

    private static final Logger log = LoggerFactory.getLogger(QualityControlService.class);

    private final QualityControlRecordRepository qualityControlRecordRepository;
    private final QualityControlMapper qualityControlMapper;
    private final LabInstrumentRepository labInstrumentRepository;

    public QualityControlService(QualityControlRecordRepository qualityControlRecordRepository,
                                 QualityControlMapper qualityControlMapper,
                                 LabInstrumentRepository labInstrumentRepository) {
        this.qualityControlRecordRepository = qualityControlRecordRepository;
        this.qualityControlMapper = qualityControlMapper;
        this.labInstrumentRepository = labInstrumentRepository;
    }

    @Transactional
    public QualityControlResponse createRecord(QualityControlCreateRequest request) {
        log.info("Creating QC record for instrumentId={}, testName={}",
                request.instrumentId(), request.testName());

        labInstrumentRepository.findById(request.instrumentId())
                .orElseThrow(() -> new ResourceNotFoundException("LabInstrument", "id", request.instrumentId()));

        QualityControlRecord record = qualityControlMapper.toEntity(request);
        record.setQcDate(Instant.now());
        record.setBranchId(request.branchId());
        record.setStatus(request.isWithinRange() ? QcStatusEnum.COMPLETED : QcStatusEnum.FAILED);

        record = qualityControlRecordRepository.save(record);
        return qualityControlMapper.toResponse(record);
    }

    public List<QualityControlResponse> getRecordsByInstrument(UUID instrumentId) {
        return qualityControlMapper.toResponseList(
                qualityControlRecordRepository.findByInstrumentId(instrumentId));
    }

    public PagedResponse<QualityControlResponse> getRecordsByBranch(UUID branchId, Pageable pageable) {
        Page<QualityControlRecord> page = qualityControlRecordRepository.findByBranchId(branchId, pageable);
        List<QualityControlResponse> content = qualityControlMapper.toResponseList(page.getContent());
        return new PagedResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    public QualityControlResponse getRecordById(UUID id) {
        QualityControlRecord record = qualityControlRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("QualityControlRecord", "id", id));
        return qualityControlMapper.toResponse(record);
    }
}
