package com.bloodbank.donorservice.repository;

import com.bloodbank.donorservice.entity.CollectionAdverseReaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CollectionAdverseReactionRepository extends JpaRepository<CollectionAdverseReaction, UUID> {

    List<CollectionAdverseReaction> findByCollectionId(UUID collectionId);
}
