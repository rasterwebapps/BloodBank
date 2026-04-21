package com.bloodbank.reportingservice.repository;

import com.bloodbank.reportingservice.entity.AuditLog;
import com.bloodbank.reportingservice.enums.AuditActionEnum;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.QueryHint;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID>,
                                            JpaSpecificationExecutor<AuditLog> {

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<AuditLog> findByEntityTypeAndEntityIdOrderByTimestampDesc(String entityType, UUID entityId);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<AuditLog> findByActorIdOrderByTimestampDesc(String actorId);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<AuditLog> findByActionOrderByTimestampDesc(AuditActionEnum action);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<AuditLog> findByBranchIdOrderByTimestampDesc(UUID branchId);
}
