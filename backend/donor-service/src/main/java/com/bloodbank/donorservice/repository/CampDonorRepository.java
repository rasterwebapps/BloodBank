package com.bloodbank.donorservice.repository;

import com.bloodbank.donorservice.entity.CampDonor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.QueryHint;

@Repository
public interface CampDonorRepository extends JpaRepository<CampDonor, UUID> {

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<CampDonor> findByCampId(UUID campId);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    Optional<CampDonor> findByCampIdAndDonorId(UUID campId, UUID donorId);

    boolean existsByCampIdAndDonorId(UUID campId, UUID donorId);
}
