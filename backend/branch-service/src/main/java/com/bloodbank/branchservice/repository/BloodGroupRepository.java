package com.bloodbank.branchservice.repository;

import com.bloodbank.branchservice.entity.BloodGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BloodGroupRepository extends JpaRepository<BloodGroup, UUID> {
    Optional<BloodGroup> findByGroupName(String groupName);
    List<BloodGroup> findByIsActiveTrue();
}
