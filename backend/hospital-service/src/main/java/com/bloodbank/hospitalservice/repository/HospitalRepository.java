package com.bloodbank.hospitalservice.repository;

import com.bloodbank.hospitalservice.entity.Hospital;
import com.bloodbank.hospitalservice.enums.HospitalStatusEnum;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface HospitalRepository extends JpaRepository<Hospital, UUID>, JpaSpecificationExecutor<Hospital> {

    Optional<Hospital> findByHospitalCode(String hospitalCode);

    boolean existsByHospitalCode(String hospitalCode);

    boolean existsByEmail(String email);

    Page<Hospital> findByStatus(HospitalStatusEnum status, Pageable pageable);

    Page<Hospital> findByHospitalNameContainingIgnoreCase(String name, Pageable pageable);
}
