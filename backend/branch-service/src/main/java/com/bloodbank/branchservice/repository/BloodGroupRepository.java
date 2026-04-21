package com.bloodbank.branchservice.repository;

import com.bloodbank.branchservice.entity.BloodGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.QueryHint;

@Repository
public interface BloodGroupRepository extends JpaRepository<BloodGroup, UUID> {
    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    Optional<BloodGroup> findByGroupName(String groupName);
    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<BloodGroup> findByIsActiveTrue();
}
