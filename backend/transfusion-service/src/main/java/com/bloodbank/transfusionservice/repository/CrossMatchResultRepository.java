package com.bloodbank.transfusionservice.repository;

import com.bloodbank.transfusionservice.entity.CrossMatchResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CrossMatchResultRepository extends JpaRepository<CrossMatchResult, UUID> {
    List<CrossMatchResult> findByCrossmatchRequestId(UUID crossmatchRequestId);
}
