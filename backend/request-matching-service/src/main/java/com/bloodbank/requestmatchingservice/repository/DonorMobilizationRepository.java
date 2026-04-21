package com.bloodbank.requestmatchingservice.repository;

import com.bloodbank.requestmatchingservice.entity.DonorMobilization;
import com.bloodbank.requestmatchingservice.enums.MobilizationStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.QueryHint;

public interface DonorMobilizationRepository extends JpaRepository<DonorMobilization, UUID>,
                                                      JpaSpecificationExecutor<DonorMobilization> {
    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<DonorMobilization> findByDisasterEventId(UUID disasterEventId);
    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<DonorMobilization> findByEmergencyRequestId(UUID emergencyRequestId);
    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<DonorMobilization> findByDonorId(UUID donorId);
    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    Page<DonorMobilization> findByDisasterEventId(UUID disasterEventId, Pageable pageable);
    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<DonorMobilization> findByDisasterEventIdAndResponse(UUID disasterEventId, MobilizationStatusEnum response);
    boolean existsByDisasterEventIdAndDonorId(UUID disasterEventId, UUID donorId);
}
