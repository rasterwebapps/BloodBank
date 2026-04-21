package com.bloodbank.transfusionservice.repository;

import com.bloodbank.transfusionservice.entity.TransfusionReaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.QueryHint;

public interface TransfusionReactionRepository extends JpaRepository<TransfusionReaction, UUID> {
    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<TransfusionReaction> findByTransfusionId(UUID transfusionId);
}
