package com.bloodbank.transfusionservice.repository;

import com.bloodbank.transfusionservice.entity.BloodIssue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.QueryHint;

public interface BloodIssueRepository extends JpaRepository<BloodIssue, UUID> {
    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    Optional<BloodIssue> findByIssueNumber(String issueNumber);
    boolean existsByIssueNumber(String issueNumber);
}
