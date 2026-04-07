package com.bloodbank.transfusionservice.repository;

import com.bloodbank.transfusionservice.entity.LookBackInvestigation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LookBackInvestigationRepository extends JpaRepository<LookBackInvestigation, UUID> {
    Optional<LookBackInvestigation> findByInvestigationNumber(String investigationNumber);
}
