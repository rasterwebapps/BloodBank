package com.bloodbank.branchservice.repository;

import com.bloodbank.branchservice.entity.ComponentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ComponentTypeRepository extends JpaRepository<ComponentType, UUID> {
    Optional<ComponentType> findByTypeCode(String typeCode);
    List<ComponentType> findByIsActiveTrue();
    boolean existsByTypeCode(String typeCode);
}
