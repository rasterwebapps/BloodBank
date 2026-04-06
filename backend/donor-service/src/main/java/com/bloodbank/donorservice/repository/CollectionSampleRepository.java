package com.bloodbank.donorservice.repository;

import com.bloodbank.donorservice.entity.CollectionSample;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CollectionSampleRepository extends JpaRepository<CollectionSample, UUID> {

    List<CollectionSample> findByCollectionId(UUID collectionId);

    Optional<CollectionSample> findBySampleNumber(String sampleNumber);

    boolean existsBySampleNumber(String sampleNumber);
}
