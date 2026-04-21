package com.bloodbank.inventoryservice.repository;

import com.bloodbank.common.model.enums.ComponentStatusEnum;
import com.bloodbank.inventoryservice.entity.BloodComponent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.QueryHint;

public interface BloodComponentRepository extends JpaRepository<BloodComponent, UUID>,
                                                  JpaSpecificationExecutor<BloodComponent> {

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<BloodComponent> findByBloodUnitId(UUID bloodUnitId);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<BloodComponent> findByStatus(ComponentStatusEnum status);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<BloodComponent> findByComponentTypeIdAndStatusOrderByExpiryDateAsc(UUID componentTypeId, ComponentStatusEnum status);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<BloodComponent> findByExpiryDateBeforeAndStatusIn(Instant expiryDate, List<ComponentStatusEnum> statuses);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    Optional<BloodComponent> findByComponentNumber(String componentNumber);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    long countByComponentTypeIdAndBloodGroupIdAndStatus(UUID componentTypeId, UUID bloodGroupId, ComponentStatusEnum status);
}
