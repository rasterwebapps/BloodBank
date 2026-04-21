package com.bloodbank.transfusionservice.repository;

import com.bloodbank.transfusionservice.entity.LookBackInvestigation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.QueryHint;

public interface LookBackInvestigationRepository extends JpaRepository<LookBackInvestigation, UUID> {
    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    Optional<LookBackInvestigation> findByInvestigationNumber(String investigationNumber);
}
