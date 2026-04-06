package com.bloodbank.inventoryservice.repository;

import com.bloodbank.inventoryservice.entity.ComponentLabel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface ComponentLabelRepository extends JpaRepository<ComponentLabel, UUID>,
                                                  JpaSpecificationExecutor<ComponentLabel> {

    List<ComponentLabel> findByComponentId(UUID componentId);
}
