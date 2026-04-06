package com.bloodbank.inventoryservice.repository;

import com.bloodbank.inventoryservice.entity.TransportBox;
import com.bloodbank.inventoryservice.enums.TransportBoxStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransportBoxRepository extends JpaRepository<TransportBox, UUID>,
                                                JpaSpecificationExecutor<TransportBox> {

    List<TransportBox> findByStatus(TransportBoxStatusEnum status);

    Optional<TransportBox> findByBoxCode(String boxCode);
}
