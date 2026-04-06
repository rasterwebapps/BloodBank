package com.bloodbank.inventoryservice.repository;

import com.bloodbank.inventoryservice.entity.StorageLocation;
import com.bloodbank.inventoryservice.enums.StorageLocationStatusEnum;
import com.bloodbank.inventoryservice.enums.StorageLocationTypeEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StorageLocationRepository extends JpaRepository<StorageLocation, UUID>,
                                                   JpaSpecificationExecutor<StorageLocation> {

    List<StorageLocation> findByLocationType(StorageLocationTypeEnum locationType);

    List<StorageLocation> findByStatus(StorageLocationStatusEnum status);

    Optional<StorageLocation> findByLocationCode(String locationCode);
}
