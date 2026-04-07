package com.bloodbank.billingservice.repository;

import com.bloodbank.billingservice.entity.RateMaster;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RateRepository extends JpaRepository<RateMaster, UUID>,
                                        JpaSpecificationExecutor<RateMaster> {

    List<RateMaster> findByBranchIdAndActiveTrue(UUID branchId);

    Optional<RateMaster> findByServiceCodeAndBranchId(String serviceCode, UUID branchId);

    List<RateMaster> findByComponentTypeIdAndBranchId(UUID componentTypeId, UUID branchId);
}
