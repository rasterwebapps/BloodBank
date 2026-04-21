package com.bloodbank.transfusionservice.repository;

import com.bloodbank.common.model.enums.RequestStatusEnum;
import com.bloodbank.transfusionservice.entity.CrossMatchRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.QueryHint;

public interface CrossMatchRequestRepository extends JpaRepository<CrossMatchRequest, UUID> {
    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    Optional<CrossMatchRequest> findByRequestNumber(String requestNumber);
    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    Page<CrossMatchRequest> findByStatus(RequestStatusEnum status, Pageable pageable);
    boolean existsByRequestNumber(String requestNumber);
}
