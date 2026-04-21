package com.bloodbank.donorservice.repository;

import com.bloodbank.donorservice.entity.CollectionAdverseReaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.QueryHint;

@Repository
public interface CollectionAdverseReactionRepository extends JpaRepository<CollectionAdverseReaction, UUID> {

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<CollectionAdverseReaction> findByCollectionId(UUID collectionId);
}
