package com.bloodbank.inventoryservice.repository;

import com.bloodbank.common.model.enums.ComponentStatusEnum;
import com.bloodbank.inventoryservice.entity.BloodComponent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BloodComponentRepository extends JpaRepository<BloodComponent, UUID>,
                                                  JpaSpecificationExecutor<BloodComponent> {

    List<BloodComponent> findByBloodUnitId(UUID bloodUnitId);

    List<BloodComponent> findByStatus(ComponentStatusEnum status);

    List<BloodComponent> findByComponentTypeIdAndStatusOrderByExpiryDateAsc(UUID componentTypeId, ComponentStatusEnum status);

    List<BloodComponent> findByExpiryDateBeforeAndStatusIn(Instant expiryDate, List<ComponentStatusEnum> statuses);

    Optional<BloodComponent> findByComponentNumber(String componentNumber);

    long countByComponentTypeIdAndBloodGroupIdAndStatus(UUID componentTypeId, UUID bloodGroupId, ComponentStatusEnum status);
}
