package com.bloodbank.reportingservice.repository;

import com.bloodbank.reportingservice.entity.ChainOfCustody;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.QueryHint;

public interface ChainOfCustodyRepository extends JpaRepository<ChainOfCustody, UUID>,
                                                  JpaSpecificationExecutor<ChainOfCustody> {

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<ChainOfCustody> findByEntityTypeAndEntityIdOrderByEventTimeAsc(String entityType, UUID entityId);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<ChainOfCustody> findByBranchIdOrderByEventTimeDesc(UUID branchId);
}
