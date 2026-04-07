package com.bloodbank.transfusionservice.service;

import com.bloodbank.common.exceptions.BusinessException;
import com.bloodbank.common.exceptions.ResourceNotFoundException;
import com.bloodbank.transfusionservice.dto.BloodIssueCreateRequest;
import com.bloodbank.transfusionservice.dto.BloodIssueResponse;
import com.bloodbank.transfusionservice.dto.EmergencyIssueCreateRequest;
import com.bloodbank.transfusionservice.dto.EmergencyIssueResponse;
import com.bloodbank.transfusionservice.entity.BloodIssue;
import com.bloodbank.transfusionservice.entity.EmergencyIssue;
import com.bloodbank.transfusionservice.enums.EmergencyTypeEnum;
import com.bloodbank.transfusionservice.enums.IssueStatusEnum;
import com.bloodbank.transfusionservice.mapper.BloodIssueMapper;
import com.bloodbank.transfusionservice.mapper.EmergencyIssueMapper;
import com.bloodbank.transfusionservice.repository.BloodIssueRepository;
import com.bloodbank.transfusionservice.repository.EmergencyIssueRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class BloodIssueService {

    private static final Logger log = LoggerFactory.getLogger(BloodIssueService.class);

    private final BloodIssueRepository bloodIssueRepository;
    private final EmergencyIssueRepository emergencyIssueRepository;
    private final BloodIssueMapper bloodIssueMapper;
    private final EmergencyIssueMapper emergencyIssueMapper;

    public BloodIssueService(BloodIssueRepository bloodIssueRepository,
                             EmergencyIssueRepository emergencyIssueRepository,
                             BloodIssueMapper bloodIssueMapper,
                             EmergencyIssueMapper emergencyIssueMapper) {
        this.bloodIssueRepository = bloodIssueRepository;
        this.emergencyIssueRepository = emergencyIssueRepository;
        this.bloodIssueMapper = bloodIssueMapper;
        this.emergencyIssueMapper = emergencyIssueMapper;
    }

    @Transactional
    public BloodIssueResponse issueBlood(BloodIssueCreateRequest request) {
        log.info("Issuing blood to patient: {}", request.patientName());

        BloodIssue entity = bloodIssueMapper.toEntity(request);
        entity.setIssueNumber("BI-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        entity.setIssueDate(Instant.now());
        entity.setStatus(IssueStatusEnum.ISSUED);
        entity.setBranchId(request.branchId());

        entity = bloodIssueRepository.save(entity);
        log.info("Blood issued with number: {}", entity.getIssueNumber());
        return bloodIssueMapper.toResponse(entity);
    }

    @Transactional
    public EmergencyIssueResponse issueEmergencyBlood(EmergencyIssueCreateRequest request) {
        log.info("Issuing emergency blood for patient: {}", request.patientName());

        BloodIssue bloodIssue = new BloodIssue();
        bloodIssue.setIssueNumber("BI-E-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        bloodIssue.setComponentId(request.componentId());
        bloodIssue.setPatientName(request.patientName());
        bloodIssue.setPatientId(request.patientId());
        bloodIssue.setHospitalId(request.hospitalId());
        bloodIssue.setIssuedTo(request.issuedTo());
        bloodIssue.setIssuedBy(request.issuedBy());
        bloodIssue.setIssueDate(Instant.now());
        bloodIssue.setStatus(IssueStatusEnum.ISSUED);
        bloodIssue.setNotes(request.notes());
        bloodIssue.setBranchId(request.branchId());

        bloodIssue = bloodIssueRepository.save(bloodIssue);

        EmergencyIssue emergencyIssue = new EmergencyIssue();
        emergencyIssue.setBloodIssueId(bloodIssue.getId());
        emergencyIssue.setEmergencyType(request.emergencyType());
        emergencyIssue.setAuthorizationBy(request.authorizationBy());
        emergencyIssue.setAuthorizationTime(Instant.now());
        emergencyIssue.setClinicalJustification(request.clinicalJustification());
        emergencyIssue.setPostCrossmatchDone(false);
        emergencyIssue.setBranchId(request.branchId());

        emergencyIssue = emergencyIssueRepository.save(emergencyIssue);
        log.info("Emergency blood issued with number: {}", bloodIssue.getIssueNumber());
        return emergencyIssueMapper.toResponse(emergencyIssue);
    }

    @Transactional
    public BloodIssueResponse returnBlood(UUID id, String returnReason) {
        log.info("Returning blood issue: {}", id);

        BloodIssue bloodIssue = bloodIssueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BloodIssue", "id", id));

        if (bloodIssue.getStatus() != IssueStatusEnum.ISSUED) {
            throw new BusinessException("Blood can only be returned when status is ISSUED", "INVALID_STATUS");
        }

        bloodIssue.setStatus(IssueStatusEnum.RETURNED);
        bloodIssue.setReturnDate(Instant.now());
        bloodIssue.setReturnReason(returnReason);

        bloodIssue = bloodIssueRepository.save(bloodIssue);
        log.info("Blood returned: {}", bloodIssue.getIssueNumber());
        return bloodIssueMapper.toResponse(bloodIssue);
    }

    public BloodIssueResponse getById(UUID id) {
        log.debug("Fetching blood issue by id: {}", id);
        BloodIssue entity = bloodIssueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BloodIssue", "id", id));
        return bloodIssueMapper.toResponse(entity);
    }

    public BloodIssueResponse getByIssueNumber(String issueNumber) {
        log.debug("Fetching blood issue by number: {}", issueNumber);
        BloodIssue entity = bloodIssueRepository.findByIssueNumber(issueNumber)
                .orElseThrow(() -> new ResourceNotFoundException("BloodIssue", "issueNumber", issueNumber));
        return bloodIssueMapper.toResponse(entity);
    }
}
