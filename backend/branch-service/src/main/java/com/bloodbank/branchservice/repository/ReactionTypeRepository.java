package com.bloodbank.branchservice.repository;

import com.bloodbank.branchservice.entity.ReactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.QueryHint;

@Repository
public interface ReactionTypeRepository extends JpaRepository<ReactionType, UUID> {
    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    Optional<ReactionType> findByReactionCode(String reactionCode);
    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<ReactionType> findByIsActiveTrue();
    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<ReactionType> findBySeverity(String severity);
    boolean existsByReactionCode(String reactionCode);
}
