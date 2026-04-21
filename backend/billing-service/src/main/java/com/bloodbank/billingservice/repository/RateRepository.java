package com.bloodbank.billingservice.repository;

import com.bloodbank.billingservice.entity.RateMaster;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.QueryHint;

@Repository
public interface RateRepository extends JpaRepository<RateMaster, UUID>,
                                        JpaSpecificationExecutor<RateMaster> {

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<RateMaster> findByBranchIdAndActiveTrue(UUID branchId);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    Optional<RateMaster> findByServiceCodeAndBranchId(String serviceCode, UUID branchId);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<RateMaster> findByComponentTypeIdAndBranchId(UUID componentTypeId, UUID branchId);
}
