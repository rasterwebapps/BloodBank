package com.bloodbank.inventoryservice.repository;

import com.bloodbank.inventoryservice.entity.StockTransfer;
import com.bloodbank.inventoryservice.enums.TransferStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StockTransferRepository extends JpaRepository<StockTransfer, UUID>,
                                                 JpaSpecificationExecutor<StockTransfer> {

    List<StockTransfer> findBySourceBranchId(UUID sourceBranchId);

    List<StockTransfer> findByDestinationBranchId(UUID destinationBranchId);

    List<StockTransfer> findByStatus(TransferStatusEnum status);

    Optional<StockTransfer> findByTransferNumber(String transferNumber);
}
