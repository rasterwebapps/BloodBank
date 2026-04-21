package com.bloodbank.transfusionservice.repository;

import com.bloodbank.transfusionservice.entity.EmergencyIssue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.QueryHint;

public interface EmergencyIssueRepository extends JpaRepository<EmergencyIssue, UUID> {
    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    Optional<EmergencyIssue> findByBloodIssueId(UUID bloodIssueId);
}
