package com.bloodbank.inventoryservice.repository;

import com.bloodbank.inventoryservice.entity.ComponentLabel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.QueryHint;

public interface ComponentLabelRepository extends JpaRepository<ComponentLabel, UUID>,
                                                  JpaSpecificationExecutor<ComponentLabel> {

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<ComponentLabel> findByComponentId(UUID componentId);
}
