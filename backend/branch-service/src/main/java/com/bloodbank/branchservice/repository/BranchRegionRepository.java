package com.bloodbank.branchservice.repository;

import com.bloodbank.branchservice.entity.BranchRegion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BranchRegionRepository extends JpaRepository<BranchRegion, UUID> {
    List<BranchRegion> findByBranchId(UUID branchId);
    List<BranchRegion> findByRegionId(UUID regionId);
    Optional<BranchRegion> findByBranchIdAndRegionId(UUID branchId, UUID regionId);
    boolean existsByBranchIdAndRegionId(UUID branchId, UUID regionId);
}
