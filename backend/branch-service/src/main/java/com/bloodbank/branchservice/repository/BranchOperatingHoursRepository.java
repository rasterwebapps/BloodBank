package com.bloodbank.branchservice.repository;

import com.bloodbank.branchservice.entity.BranchOperatingHours;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.QueryHint;

@Repository
public interface BranchOperatingHoursRepository extends JpaRepository<BranchOperatingHours, UUID> {
    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<BranchOperatingHours> findByBranchId(UUID branchId);
    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    Optional<BranchOperatingHours> findByBranchIdAndDayOfWeek(UUID branchId, String dayOfWeek);
    void deleteByBranchId(UUID branchId);
}
