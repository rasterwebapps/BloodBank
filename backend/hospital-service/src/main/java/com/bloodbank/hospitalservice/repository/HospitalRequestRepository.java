package com.bloodbank.hospitalservice.repository;

import com.bloodbank.hospitalservice.entity.HospitalRequest;
import com.bloodbank.hospitalservice.enums.HospitalRequestStatusEnum;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HospitalRequestRepository extends JpaRepository<HospitalRequest, UUID>,
                                                    JpaSpecificationExecutor<HospitalRequest> {

    Optional<HospitalRequest> findByRequestNumber(String requestNumber);

    boolean existsByRequestNumber(String requestNumber);

    Page<HospitalRequest> findByHospitalId(UUID hospitalId, Pageable pageable);

    Page<HospitalRequest> findByStatus(HospitalRequestStatusEnum status, Pageable pageable);

    List<HospitalRequest> findByHospitalIdAndStatus(UUID hospitalId, HospitalRequestStatusEnum status);
}
