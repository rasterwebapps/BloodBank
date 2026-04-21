package com.bloodbank.requestmatchingservice.repository;

import com.bloodbank.requestmatchingservice.entity.EmergencyRequest;
import com.bloodbank.requestmatchingservice.enums.EmergencyPriorityEnum;
import com.bloodbank.requestmatchingservice.enums.EmergencyStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.QueryHint;

public interface EmergencyRequestRepository extends JpaRepository<EmergencyRequest, UUID>,
                                                     JpaSpecificationExecutor<EmergencyRequest> {
    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    Optional<EmergencyRequest> findByRequestNumber(String requestNumber);
    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    Page<EmergencyRequest> findByStatus(EmergencyStatusEnum status, Pageable pageable);
    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<EmergencyRequest> findByStatusAndPriority(EmergencyStatusEnum status, EmergencyPriorityEnum priority);
    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<EmergencyRequest> findByDisasterEventId(UUID disasterEventId);
    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<EmergencyRequest> findByHospitalId(UUID hospitalId);
    boolean existsByRequestNumber(String requestNumber);
}
