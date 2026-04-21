package com.bloodbank.branchservice.repository;

import com.bloodbank.branchservice.entity.Branch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.QueryHint;

@Repository
public interface BranchRepository extends JpaRepository<Branch, UUID>, JpaSpecificationExecutor<Branch> {
    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    Optional<Branch> findByBranchCode(String branchCode);
    boolean existsByBranchCode(String branchCode);
    boolean existsByEmail(String email);
    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<Branch> findByStatus(String status);
    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    Page<Branch> findByStatus(String status, Pageable pageable);
    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<Branch> findByBranchType(String branchType);
    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<Branch> findByParentBranchId(UUID parentBranchId);
    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    Page<Branch> findByBranchNameContainingIgnoreCase(String name, Pageable pageable);
}
