package com.bloodbank.inventoryservice.repository;

import com.bloodbank.common.model.enums.BloodUnitStatusEnum;
import com.bloodbank.inventoryservice.entity.BloodUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.QueryHint;

public interface BloodUnitRepository extends JpaRepository<BloodUnit, UUID>,
                                             JpaSpecificationExecutor<BloodUnit> {

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<BloodUnit> findByStatus(BloodUnitStatusEnum status);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<BloodUnit> findByDonorId(UUID donorId);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<BloodUnit> findByBloodGroupIdAndStatusOrderByExpiryDateAsc(UUID bloodGroupId, BloodUnitStatusEnum status);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<BloodUnit> findByExpiryDateBeforeAndStatusIn(Instant expiryDate, List<BloodUnitStatusEnum> statuses);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    Optional<BloodUnit> findByUnitNumber(String unitNumber);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    long countByBloodGroupIdAndStatus(UUID bloodGroupId, BloodUnitStatusEnum status);
}
