package com.bloodbank.complianceservice.repository;

import com.bloodbank.complianceservice.entity.RegulatoryFramework;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RegulatoryFrameworkRepository extends JpaRepository<RegulatoryFramework, UUID>,
                                                       JpaSpecificationExecutor<RegulatoryFramework> {

    Optional<RegulatoryFramework> findByFrameworkCode(String frameworkCode);

    List<RegulatoryFramework> findByIsActive(boolean isActive);
}
