package com.bloodbank.inventoryservice.repository;

import com.bloodbank.inventoryservice.entity.ComponentProcessing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface ComponentProcessingRepository extends JpaRepository<ComponentProcessing, UUID>,
                                                       JpaSpecificationExecutor<ComponentProcessing> {

    List<ComponentProcessing> findByComponentId(UUID componentId);
}
