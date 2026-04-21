package com.bloodbank.donorservice.repository;

import com.bloodbank.common.model.enums.DonorStatusEnum;
import com.bloodbank.donorservice.entity.Donor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import jakarta.persistence.QueryHint;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DonorRepository extends JpaRepository<Donor, UUID>, JpaSpecificationExecutor<Donor> {

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    Optional<Donor> findByDonorNumber(String donorNumber);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    Optional<Donor> findByEmail(String email);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    Optional<Donor> findByNationalId(String nationalId);

    boolean existsByDonorNumber(String donorNumber);

    boolean existsByEmail(String email);

    boolean existsByNationalId(String nationalId);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<Donor> findByBloodGroupIdAndStatus(UUID bloodGroupId, DonorStatusEnum status);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    Page<Donor> findByStatus(DonorStatusEnum status, Pageable pageable);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    Page<Donor> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String firstName, String lastName, Pageable pageable);
}
