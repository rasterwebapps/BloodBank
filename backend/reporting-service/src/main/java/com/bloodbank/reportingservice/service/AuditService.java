package com.bloodbank.reportingservice.service;

import com.bloodbank.reportingservice.dto.AuditLogCreateRequest;
import com.bloodbank.reportingservice.dto.AuditLogResponse;
import com.bloodbank.reportingservice.entity.AuditLog;
import com.bloodbank.reportingservice.enums.AuditActionEnum;
import com.bloodbank.reportingservice.mapper.AuditLogMapper;
import com.bloodbank.reportingservice.repository.AuditLogRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final AuditLogRepository auditLogRepository;
    private final AuditLogMapper auditLogMapper;

    public AuditService(AuditLogRepository auditLogRepository, AuditLogMapper auditLogMapper) {
        this.auditLogRepository = auditLogRepository;
        this.auditLogMapper = auditLogMapper;
    }

    @Transactional
    public AuditLogResponse createAuditLog(AuditLogCreateRequest request) {
        log.info("Creating audit log for entity {} action {}", request.entityType(), request.action());
        AuditLog auditLog = auditLogMapper.toEntity(request);
        auditLog = auditLogRepository.save(auditLog);
        return auditLogMapper.toResponse(auditLog);
    }

    public List<AuditLogResponse> getByEntityId(String entityType, UUID entityId) {
        log.debug("Fetching audit logs for {} with id {}", entityType, entityId);
        List<AuditLog> logs = auditLogRepository.findByEntityTypeAndEntityIdOrderByTimestampDesc(entityType, entityId);
        return auditLogMapper.toResponseList(logs);
    }

    public List<AuditLogResponse> getByActorId(String actorId) {
        log.debug("Fetching audit logs for actor {}", actorId);
        List<AuditLog> logs = auditLogRepository.findByActorIdOrderByTimestampDesc(actorId);
        return auditLogMapper.toResponseList(logs);
    }

    public List<AuditLogResponse> getByAction(AuditActionEnum action) {
        log.debug("Fetching audit logs for action {}", action);
        List<AuditLog> logs = auditLogRepository.findByActionOrderByTimestampDesc(action);
        return auditLogMapper.toResponseList(logs);
    }
}
