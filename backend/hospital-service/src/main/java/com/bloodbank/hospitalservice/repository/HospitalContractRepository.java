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
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.QueryHint;

@Repository
public interface HospitalContractRepository extends JpaRepository<HospitalContract, UUID> {

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<HospitalContract> findByHospitalId(UUID hospitalId);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    Page<HospitalContract> findByHospitalId(UUID hospitalId, Pageable pageable);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    Optional<HospitalContract> findByContractNumber(String contractNumber);

    boolean existsByContractNumber(String contractNumber);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<HospitalContract> findByHospitalIdAndStatus(UUID hospitalId, ContractStatusEnum status);
}
