package com.bloodbank.inventoryservice.repository;

import com.bloodbank.common.model.enums.ComponentStatusEnum;
import com.bloodbank.inventoryservice.entity.PooledComponent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.QueryHint;

public interface PooledComponentRepository extends JpaRepository<PooledComponent, UUID>,
                                                   JpaSpecificationExecutor<PooledComponent> {

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<PooledComponent> findByStatus(ComponentStatusEnum status);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    Optional<PooledComponent> findByPoolNumber(String poolNumber);
}
