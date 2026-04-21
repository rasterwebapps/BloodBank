package com.bloodbank.donorservice.repository;

import com.bloodbank.donorservice.entity.CampCollection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.QueryHint;

@Repository
public interface CampCollectionRepository extends JpaRepository<CampCollection, UUID> {

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<CampCollection> findByCampId(UUID campId);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    Optional<CampCollection> findByCampIdAndCollectionId(UUID campId, UUID collectionId);

    boolean existsByCampIdAndCollectionId(UUID campId, UUID collectionId);
}
