package com.bloodbank.branchservice.repository;

import com.bloodbank.branchservice.entity.BranchEquipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.QueryHint;

@Repository
public interface BranchEquipmentRepository extends JpaRepository<BranchEquipment, UUID> {
    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<BranchEquipment> findByBranchId(UUID branchId);
    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<BranchEquipment> findByBranchIdAndStatus(UUID branchId, String status);
    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<BranchEquipment> findByBranchIdAndEquipmentType(UUID branchId, String equipmentType);
    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<BranchEquipment> findByNextMaintenanceDateBefore(LocalDate date);
}
