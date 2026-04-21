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
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.QueryHint;

@Repository
public interface CollectionRepository extends JpaRepository<Collection, UUID>, JpaSpecificationExecutor<Collection> {

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    Optional<Collection> findByCollectionNumber(String collectionNumber);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<Collection> findByDonorId(UUID donorId);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<Collection> findByDonorIdOrderByCollectionDateDesc(UUID donorId);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    Page<Collection> findByStatus(CollectionStatusEnum status, Pageable pageable);

    boolean existsByCollectionNumber(String collectionNumber);
}
