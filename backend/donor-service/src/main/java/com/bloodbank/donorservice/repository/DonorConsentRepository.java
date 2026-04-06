package com.bloodbank.donorservice.repository;

import com.bloodbank.donorservice.entity.DonorConsent;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DonorConsentRepository extends JpaRepository<DonorConsent, UUID> {

    List<DonorConsent> findByDonorId(UUID donorId);

    Optional<DonorConsent> findByDonorIdAndConsentType(UUID donorId, String consentType);
}
