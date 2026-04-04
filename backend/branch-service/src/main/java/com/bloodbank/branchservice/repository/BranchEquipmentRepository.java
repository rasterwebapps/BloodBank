package com.bloodbank.branchservice.repository;

import com.bloodbank.branchservice.entity.BranchEquipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface BranchEquipmentRepository extends JpaRepository<BranchEquipment, UUID> {
    List<BranchEquipment> findByBranchId(UUID branchId);
    List<BranchEquipment> findByBranchIdAndStatus(UUID branchId, String status);
    List<BranchEquipment> findByBranchIdAndEquipmentType(UUID branchId, String equipmentType);
    List<BranchEquipment> findByNextMaintenanceDateBefore(LocalDate date);
}
