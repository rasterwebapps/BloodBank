package com.bloodbank.donorservice.repository;

import com.bloodbank.donorservice.entity.CollectionSample;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.QueryHint;

@Repository
public interface CollectionSampleRepository extends JpaRepository<CollectionSample, UUID> {

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<CollectionSample> findByCollectionId(UUID collectionId);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    Optional<CollectionSample> findBySampleNumber(String sampleNumber);

    boolean existsBySampleNumber(String sampleNumber);
}
