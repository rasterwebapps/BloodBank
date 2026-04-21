package com.bloodbank.donorservice.repository;

import com.bloodbank.donorservice.entity.DonorLoyalty;
import com.bloodbank.donorservice.enums.LoyaltyTierEnum;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.QueryHint;

@Repository
public interface DonorLoyaltyRepository extends JpaRepository<DonorLoyalty, UUID> {

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    Optional<DonorLoyalty> findByDonorId(UUID donorId);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<DonorLoyalty> findByTier(LoyaltyTierEnum tier);
}
