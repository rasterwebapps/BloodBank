package com.bloodbank.donorservice.repository;

import com.bloodbank.donorservice.entity.DonorDeferral;
import com.bloodbank.donorservice.enums.DeferralStatusEnum;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DonorDeferralRepository extends JpaRepository<DonorDeferral, UUID> {

    List<DonorDeferral> findByDonorId(UUID donorId);

    List<DonorDeferral> findByDonorIdAndStatus(UUID donorId, DeferralStatusEnum status);

    boolean existsByDonorIdAndStatus(UUID donorId, DeferralStatusEnum status);
}
