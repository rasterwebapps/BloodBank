package com.bloodbank.inventoryservice.repository;

import com.bloodbank.inventoryservice.entity.ComponentProcessing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.QueryHint;

public interface ComponentProcessingRepository extends JpaRepository<ComponentProcessing, UUID>,
                                                       JpaSpecificationExecutor<ComponentProcessing> {

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<ComponentProcessing> findByComponentId(UUID componentId);
}
