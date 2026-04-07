package com.bloodbank.transfusionservice.repository;

import com.bloodbank.transfusionservice.entity.BloodIssue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BloodIssueRepository extends JpaRepository<BloodIssue, UUID> {
    Optional<BloodIssue> findByIssueNumber(String issueNumber);
    boolean existsByIssueNumber(String issueNumber);
}
