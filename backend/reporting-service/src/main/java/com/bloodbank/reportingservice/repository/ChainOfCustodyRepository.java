package com.bloodbank.reportingservice.repository;

import com.bloodbank.reportingservice.entity.ChainOfCustody;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface ChainOfCustodyRepository extends JpaRepository<ChainOfCustody, UUID>,
                                                  JpaSpecificationExecutor<ChainOfCustody> {

    List<ChainOfCustody> findByEntityTypeAndEntityIdOrderByEventTimeAsc(String entityType, UUID entityId);

    List<ChainOfCustody> findByBranchIdOrderByEventTimeDesc(UUID branchId);
}
