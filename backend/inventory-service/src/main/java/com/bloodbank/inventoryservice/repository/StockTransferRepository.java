package com.bloodbank.inventoryservice.repository;

import com.bloodbank.inventoryservice.entity.StockTransfer;
import com.bloodbank.inventoryservice.enums.TransferStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.QueryHint;

public interface StockTransferRepository extends JpaRepository<StockTransfer, UUID>,
                                                 JpaSpecificationExecutor<StockTransfer> {

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<StockTransfer> findBySourceBranchId(UUID sourceBranchId);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<StockTransfer> findByDestinationBranchId(UUID destinationBranchId);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<StockTransfer> findByStatus(TransferStatusEnum status);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    Optional<StockTransfer> findByTransferNumber(String transferNumber);
}
