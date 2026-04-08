package com.bloodbank.hospitalservice.repository;

import com.bloodbank.hospitalservice.entity.HospitalContract;
import com.bloodbank.hospitalservice.enums.ContractStatusEnum;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HospitalContractRepository extends JpaRepository<HospitalContract, UUID> {

    List<HospitalContract> findByHospitalId(UUID hospitalId);

    Page<HospitalContract> findByHospitalId(UUID hospitalId, Pageable pageable);

    Optional<HospitalContract> findByContractNumber(String contractNumber);

    boolean existsByContractNumber(String contractNumber);

    List<HospitalContract> findByHospitalIdAndStatus(UUID hospitalId, ContractStatusEnum status);
}
