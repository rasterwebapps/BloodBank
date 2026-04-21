package com.bloodbank.transfusionservice.repository;

import com.bloodbank.transfusionservice.entity.CrossMatchResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.QueryHint;

public interface CrossMatchResultRepository extends JpaRepository<CrossMatchResult, UUID> {
    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<CrossMatchResult> findByCrossmatchRequestId(UUID crossmatchRequestId);
}
