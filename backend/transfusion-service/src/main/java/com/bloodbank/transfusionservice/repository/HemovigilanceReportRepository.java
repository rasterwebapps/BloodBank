package com.bloodbank.transfusionservice.repository;

import com.bloodbank.transfusionservice.entity.HemovigilanceReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface HemovigilanceReportRepository extends JpaRepository<HemovigilanceReport, UUID> {
    Optional<HemovigilanceReport> findByReportNumber(String reportNumber);
    Optional<HemovigilanceReport> findByTransfusionReactionId(UUID transfusionReactionId);
}
