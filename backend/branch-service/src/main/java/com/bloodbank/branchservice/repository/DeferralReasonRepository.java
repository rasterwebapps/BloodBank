package com.bloodbank.branchservice.repository;

import com.bloodbank.branchservice.entity.DeferralReason;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeferralReasonRepository extends JpaRepository<DeferralReason, UUID> {
    Optional<DeferralReason> findByReasonCode(String reasonCode);
    List<DeferralReason> findByIsActiveTrue();
    List<DeferralReason> findByDeferralType(String deferralType);
    boolean existsByReasonCode(String reasonCode);
}
