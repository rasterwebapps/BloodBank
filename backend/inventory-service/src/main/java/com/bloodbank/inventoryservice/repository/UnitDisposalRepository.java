package com.bloodbank.inventoryservice.repository;

import com.bloodbank.inventoryservice.entity.UnitDisposal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface UnitDisposalRepository extends JpaRepository<UnitDisposal, UUID>,
                                                JpaSpecificationExecutor<UnitDisposal> {

    List<UnitDisposal> findByBloodUnitId(UUID bloodUnitId);

    List<UnitDisposal> findByComponentId(UUID componentId);
}
