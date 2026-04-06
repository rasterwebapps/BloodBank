package com.bloodbank.branchservice.repository;

import com.bloodbank.branchservice.entity.ReactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReactionTypeRepository extends JpaRepository<ReactionType, UUID> {
    Optional<ReactionType> findByReactionCode(String reactionCode);
    List<ReactionType> findByIsActiveTrue();
    List<ReactionType> findBySeverity(String severity);
    boolean existsByReactionCode(String reactionCode);
}
