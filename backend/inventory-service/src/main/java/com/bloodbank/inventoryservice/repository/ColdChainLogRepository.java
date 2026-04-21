package com.bloodbank.inventoryservice.repository;

import com.bloodbank.inventoryservice.entity.ColdChainLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.QueryHint;

public interface ColdChainLogRepository extends JpaRepository<ColdChainLog, UUID>,
                                                JpaSpecificationExecutor<ColdChainLog> {

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<ColdChainLog> findByTransportRequestId(UUID transportRequestId);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<ColdChainLog> findByStorageLocationId(UUID storageLocationId);
}
