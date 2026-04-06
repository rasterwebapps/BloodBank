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

@Repository
public interface BranchRepository extends JpaRepository<Branch, UUID>, JpaSpecificationExecutor<Branch> {
    Optional<Branch> findByBranchCode(String branchCode);
    boolean existsByBranchCode(String branchCode);
    boolean existsByEmail(String email);
    List<Branch> findByStatus(String status);
    Page<Branch> findByStatus(String status, Pageable pageable);
    List<Branch> findByBranchType(String branchType);
    List<Branch> findByParentBranchId(UUID parentBranchId);
    Page<Branch> findByBranchNameContainingIgnoreCase(String name, Pageable pageable);
}
