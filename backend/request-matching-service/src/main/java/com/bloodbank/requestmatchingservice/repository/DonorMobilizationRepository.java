package com.bloodbank.requestmatchingservice.repository;

import com.bloodbank.requestmatchingservice.entity.DonorMobilization;
import com.bloodbank.requestmatchingservice.enums.MobilizationStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface DonorMobilizationRepository extends JpaRepository<DonorMobilization, UUID>,
                                                      JpaSpecificationExecutor<DonorMobilization> {
    List<DonorMobilization> findByDisasterEventId(UUID disasterEventId);
    List<DonorMobilization> findByEmergencyRequestId(UUID emergencyRequestId);
    List<DonorMobilization> findByDonorId(UUID donorId);
    Page<DonorMobilization> findByDisasterEventId(UUID disasterEventId, Pageable pageable);
    List<DonorMobilization> findByDisasterEventIdAndResponse(UUID disasterEventId, MobilizationStatusEnum response);
    boolean existsByDisasterEventIdAndDonorId(UUID disasterEventId, UUID donorId);
}
