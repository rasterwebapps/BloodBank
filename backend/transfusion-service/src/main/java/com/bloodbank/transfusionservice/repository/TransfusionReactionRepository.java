package com.bloodbank.transfusionservice.repository;

import com.bloodbank.transfusionservice.entity.TransfusionReaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TransfusionReactionRepository extends JpaRepository<TransfusionReaction, UUID> {
    List<TransfusionReaction> findByTransfusionId(UUID transfusionId);
}
