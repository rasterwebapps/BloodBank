package com.bloodbank.inventoryservice.repository;

import com.bloodbank.common.model.enums.ComponentStatusEnum;
import com.bloodbank.inventoryservice.entity.PooledComponent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PooledComponentRepository extends JpaRepository<PooledComponent, UUID>,
                                                   JpaSpecificationExecutor<PooledComponent> {

    List<PooledComponent> findByStatus(ComponentStatusEnum status);

    Optional<PooledComponent> findByPoolNumber(String poolNumber);
}
