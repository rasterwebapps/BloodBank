package com.bloodbank.branchservice.repository;

import com.bloodbank.branchservice.entity.DeferralReason;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.QueryHint;

@Repository
public interface DeferralReasonRepository extends JpaRepository<DeferralReason, UUID> {
    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    Optional<DeferralReason> findByReasonCode(String reasonCode);
    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<DeferralReason> findByIsActiveTrue();
    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<DeferralReason> findByDeferralType(String deferralType);
    boolean existsByReasonCode(String reasonCode);
}
