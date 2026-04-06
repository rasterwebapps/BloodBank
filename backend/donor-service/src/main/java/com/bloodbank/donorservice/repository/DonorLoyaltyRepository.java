package com.bloodbank.donorservice.repository;

import com.bloodbank.donorservice.entity.DonorLoyalty;
import com.bloodbank.donorservice.enums.LoyaltyTierEnum;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DonorLoyaltyRepository extends JpaRepository<DonorLoyalty, UUID> {

    Optional<DonorLoyalty> findByDonorId(UUID donorId);

    List<DonorLoyalty> findByTier(LoyaltyTierEnum tier);
}
