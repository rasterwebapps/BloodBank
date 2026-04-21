package com.bloodbank.donorservice.repository;

import com.bloodbank.donorservice.entity.BloodCamp;
import com.bloodbank.donorservice.enums.CampStatusEnum;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.QueryHint;

@Repository
public interface BloodCampRepository extends JpaRepository<BloodCamp, UUID>, JpaSpecificationExecutor<BloodCamp> {

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    Optional<BloodCamp> findByCampCode(String campCode);

    boolean existsByCampCode(String campCode);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    Page<BloodCamp> findByStatus(CampStatusEnum status, Pageable pageable);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<BloodCamp> findByScheduledDateBetween(LocalDate start, LocalDate end);
}
