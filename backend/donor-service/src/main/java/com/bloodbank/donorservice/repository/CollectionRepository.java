package com.bloodbank.donorservice.repository;

import com.bloodbank.common.model.enums.CollectionStatusEnum;
import com.bloodbank.donorservice.entity.Collection;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CollectionRepository extends JpaRepository<Collection, UUID>, JpaSpecificationExecutor<Collection> {

    Optional<Collection> findByCollectionNumber(String collectionNumber);

    List<Collection> findByDonorId(UUID donorId);

    List<Collection> findByDonorIdOrderByCollectionDateDesc(UUID donorId);

    Page<Collection> findByStatus(CollectionStatusEnum status, Pageable pageable);

    boolean existsByCollectionNumber(String collectionNumber);
}
