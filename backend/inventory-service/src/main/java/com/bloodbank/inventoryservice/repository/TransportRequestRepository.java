package com.bloodbank.inventoryservice.repository;

import com.bloodbank.inventoryservice.entity.TransportRequest;
import com.bloodbank.inventoryservice.enums.TransportStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransportRequestRepository extends JpaRepository<TransportRequest, UUID>,
                                                    JpaSpecificationExecutor<TransportRequest> {

    List<TransportRequest> findByStatus(TransportStatusEnum status);

    Optional<TransportRequest> findByRequestNumber(String requestNumber);

    List<TransportRequest> findBySourceBranchId(UUID sourceBranchId);
}
