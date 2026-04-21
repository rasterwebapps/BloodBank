package com.bloodbank.transfusionservice.repository;

import com.bloodbank.transfusionservice.entity.HemovigilanceReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.QueryHint;

public interface HemovigilanceReportRepository extends JpaRepository<HemovigilanceReport, UUID> {
    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    Optional<HemovigilanceReport> findByReportNumber(String reportNumber);
    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    Optional<HemovigilanceReport> findByTransfusionReactionId(UUID transfusionReactionId);
}
