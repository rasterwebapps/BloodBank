package com.bloodbank.transfusionservice.repository;

import com.bloodbank.common.model.enums.RequestStatusEnum;
import com.bloodbank.transfusionservice.entity.CrossMatchRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CrossMatchRequestRepository extends JpaRepository<CrossMatchRequest, UUID> {
    Optional<CrossMatchRequest> findByRequestNumber(String requestNumber);
    Page<CrossMatchRequest> findByStatus(RequestStatusEnum status, Pageable pageable);
    boolean existsByRequestNumber(String requestNumber);
}
