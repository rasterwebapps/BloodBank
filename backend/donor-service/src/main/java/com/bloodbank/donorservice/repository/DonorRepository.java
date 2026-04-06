package com.bloodbank.donorservice.repository;

import com.bloodbank.common.model.enums.DonorStatusEnum;
import com.bloodbank.donorservice.entity.Donor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DonorRepository extends JpaRepository<Donor, UUID>, JpaSpecificationExecutor<Donor> {

    Optional<Donor> findByDonorNumber(String donorNumber);

    Optional<Donor> findByEmail(String email);

    Optional<Donor> findByNationalId(String nationalId);

    boolean existsByDonorNumber(String donorNumber);

    boolean existsByEmail(String email);

    boolean existsByNationalId(String nationalId);

    List<Donor> findByBloodGroupIdAndStatus(UUID bloodGroupId, DonorStatusEnum status);

    Page<Donor> findByStatus(DonorStatusEnum status, Pageable pageable);

    Page<Donor> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String firstName, String lastName, Pageable pageable);
}
