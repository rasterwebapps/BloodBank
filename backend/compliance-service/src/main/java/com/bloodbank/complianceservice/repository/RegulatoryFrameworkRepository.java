package com.bloodbank.complianceservice.repository;

import com.bloodbank.complianceservice.entity.RegulatoryFramework;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.QueryHint;

@Repository
public interface RegulatoryFrameworkRepository extends JpaRepository<RegulatoryFramework, UUID>,
                                                       JpaSpecificationExecutor<RegulatoryFramework> {

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    Optional<RegulatoryFramework> findByFrameworkCode(String frameworkCode);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<RegulatoryFramework> findByIsActive(boolean isActive);
}
