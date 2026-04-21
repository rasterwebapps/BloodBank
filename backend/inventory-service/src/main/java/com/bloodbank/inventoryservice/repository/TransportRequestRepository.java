package com.bloodbank.inventoryservice.repository;

import com.bloodbank.inventoryservice.entity.TransportRequest;
import com.bloodbank.inventoryservice.enums.TransportStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.QueryHint;

public interface TransportRequestRepository extends JpaRepository<TransportRequest, UUID>,
                                                    JpaSpecificationExecutor<TransportRequest> {

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<TransportRequest> findByStatus(TransportStatusEnum status);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    Optional<TransportRequest> findByRequestNumber(String requestNumber);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<TransportRequest> findBySourceBranchId(UUID sourceBranchId);
}
