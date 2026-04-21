package com.bloodbank.branchservice.repository;

import com.bloodbank.branchservice.entity.BranchRegion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.QueryHint;

@Repository
public interface BranchRegionRepository extends JpaRepository<BranchRegion, UUID> {
    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<BranchRegion> findByBranchId(UUID branchId);
    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<BranchRegion> findByRegionId(UUID regionId);
    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    Optional<BranchRegion> findByBranchIdAndRegionId(UUID branchId, UUID regionId);
    boolean existsByBranchIdAndRegionId(UUID branchId, UUID regionId);
}
