package com.bloodbank.branchservice.repository;

import com.bloodbank.branchservice.entity.BranchOperatingHours;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BranchOperatingHoursRepository extends JpaRepository<BranchOperatingHours, UUID> {
    List<BranchOperatingHours> findByBranchId(UUID branchId);
    Optional<BranchOperatingHours> findByBranchIdAndDayOfWeek(UUID branchId, String dayOfWeek);
    void deleteByBranchId(UUID branchId);
}
