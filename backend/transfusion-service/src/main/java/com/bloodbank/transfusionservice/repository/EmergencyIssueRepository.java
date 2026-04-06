package com.bloodbank.transfusionservice.repository;

import com.bloodbank.transfusionservice.entity.EmergencyIssue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EmergencyIssueRepository extends JpaRepository<EmergencyIssue, UUID> {
    Optional<EmergencyIssue> findByBloodIssueId(UUID bloodIssueId);
}
