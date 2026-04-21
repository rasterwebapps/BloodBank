package com.bloodbank.inventoryservice.repository;

import com.bloodbank.inventoryservice.entity.UnitDisposal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.QueryHint;

public interface UnitDisposalRepository extends JpaRepository<UnitDisposal, UUID>,
                                                JpaSpecificationExecutor<UnitDisposal> {

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<UnitDisposal> findByBloodUnitId(UUID bloodUnitId);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<UnitDisposal> findByComponentId(UUID componentId);
}
