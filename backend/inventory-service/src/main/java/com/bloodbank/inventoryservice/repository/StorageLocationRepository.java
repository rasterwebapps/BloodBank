package com.bloodbank.inventoryservice.repository;

import com.bloodbank.inventoryservice.entity.StorageLocation;
import com.bloodbank.inventoryservice.enums.StorageLocationStatusEnum;
import com.bloodbank.inventoryservice.enums.StorageLocationTypeEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.QueryHint;

public interface StorageLocationRepository extends JpaRepository<StorageLocation, UUID>,
                                                   JpaSpecificationExecutor<StorageLocation> {

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<StorageLocation> findByLocationType(StorageLocationTypeEnum locationType);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<StorageLocation> findByStatus(StorageLocationStatusEnum status);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    Optional<StorageLocation> findByLocationCode(String locationCode);
}
