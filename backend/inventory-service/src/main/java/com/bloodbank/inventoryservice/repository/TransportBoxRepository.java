package com.bloodbank.inventoryservice.repository;

import com.bloodbank.inventoryservice.entity.TransportBox;
import com.bloodbank.inventoryservice.enums.TransportBoxStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.QueryHint;

public interface TransportBoxRepository extends JpaRepository<TransportBox, UUID>,
                                                JpaSpecificationExecutor<TransportBox> {

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<TransportBox> findByStatus(TransportBoxStatusEnum status);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    Optional<TransportBox> findByBoxCode(String boxCode);
}
