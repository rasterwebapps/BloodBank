package com.bloodbank.branchservice.repository;

import com.bloodbank.branchservice.entity.ComponentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.QueryHint;

@Repository
public interface ComponentTypeRepository extends JpaRepository<ComponentType, UUID> {
    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    Optional<ComponentType> findByTypeCode(String typeCode);
    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<ComponentType> findByIsActiveTrue();
    boolean existsByTypeCode(String typeCode);
}
