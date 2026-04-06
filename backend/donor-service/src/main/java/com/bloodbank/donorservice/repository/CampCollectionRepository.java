package com.bloodbank.donorservice.repository;

import com.bloodbank.donorservice.entity.CampCollection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CampCollectionRepository extends JpaRepository<CampCollection, UUID> {

    List<CampCollection> findByCampId(UUID campId);

    Optional<CampCollection> findByCampIdAndCollectionId(UUID campId, UUID collectionId);

    boolean existsByCampIdAndCollectionId(UUID campId, UUID collectionId);
}
