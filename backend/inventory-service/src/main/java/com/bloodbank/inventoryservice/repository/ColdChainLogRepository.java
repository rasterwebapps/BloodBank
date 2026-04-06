package com.bloodbank.inventoryservice.repository;

import com.bloodbank.inventoryservice.entity.ColdChainLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface ColdChainLogRepository extends JpaRepository<ColdChainLog, UUID>,
                                                JpaSpecificationExecutor<ColdChainLog> {

    List<ColdChainLog> findByTransportRequestId(UUID transportRequestId);

    List<ColdChainLog> findByStorageLocationId(UUID storageLocationId);
}
