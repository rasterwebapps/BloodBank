package com.bloodbank.donorservice.repository;

import com.bloodbank.donorservice.entity.DonorConsent;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.QueryHint;

@Repository
public interface DonorConsentRepository extends JpaRepository<DonorConsent, UUID> {

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<DonorConsent> findByDonorId(UUID donorId);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    Optional<DonorConsent> findByDonorIdAndConsentType(UUID donorId, String consentType);
}
