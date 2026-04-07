package com.bloodbank.reportingservice.repository;

import com.bloodbank.reportingservice.entity.AuditLog;
import com.bloodbank.reportingservice.enums.AuditActionEnum;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID>,
                                            JpaSpecificationExecutor<AuditLog> {

    List<AuditLog> findByEntityTypeAndEntityIdOrderByTimestampDesc(String entityType, UUID entityId);

    List<AuditLog> findByActorIdOrderByTimestampDesc(String actorId);

    List<AuditLog> findByActionOrderByTimestampDesc(AuditActionEnum action);

    List<AuditLog> findByBranchIdOrderByTimestampDesc(UUID branchId);
}
