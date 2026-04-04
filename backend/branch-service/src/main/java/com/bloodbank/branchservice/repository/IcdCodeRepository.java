package com.bloodbank.branchservice.repository;

import com.bloodbank.branchservice.entity.IcdCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IcdCodeRepository extends JpaRepository<IcdCode, UUID> {
    Optional<IcdCode> findByIcdCode(String icdCode);
    List<IcdCode> findByIsActiveTrue();
    List<IcdCode> findByCategory(String category);
    boolean existsByIcdCode(String icdCode);
}
