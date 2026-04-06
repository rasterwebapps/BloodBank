package com.bloodbank.inventoryservice.repository;

import com.bloodbank.common.model.enums.BloodUnitStatusEnum;
import com.bloodbank.inventoryservice.entity.BloodUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BloodUnitRepository extends JpaRepository<BloodUnit, UUID>,
                                             JpaSpecificationExecutor<BloodUnit> {

    List<BloodUnit> findByStatus(BloodUnitStatusEnum status);

    List<BloodUnit> findByDonorId(UUID donorId);

    List<BloodUnit> findByBloodGroupIdAndStatusOrderByExpiryDateAsc(UUID bloodGroupId, BloodUnitStatusEnum status);

    List<BloodUnit> findByExpiryDateBeforeAndStatusIn(Instant expiryDate, List<BloodUnitStatusEnum> statuses);

    Optional<BloodUnit> findByUnitNumber(String unitNumber);

    long countByBloodGroupIdAndStatus(UUID bloodGroupId, BloodUnitStatusEnum status);
}
