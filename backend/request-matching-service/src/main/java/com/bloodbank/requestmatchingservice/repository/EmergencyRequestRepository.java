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

public interface EmergencyRequestRepository extends JpaRepository<EmergencyRequest, UUID>,
                                                     JpaSpecificationExecutor<EmergencyRequest> {
    Optional<EmergencyRequest> findByRequestNumber(String requestNumber);
    Page<EmergencyRequest> findByStatus(EmergencyStatusEnum status, Pageable pageable);
    List<EmergencyRequest> findByStatusAndPriority(EmergencyStatusEnum status, EmergencyPriorityEnum priority);
    List<EmergencyRequest> findByDisasterEventId(UUID disasterEventId);
    List<EmergencyRequest> findByHospitalId(UUID hospitalId);
    boolean existsByRequestNumber(String requestNumber);
}
