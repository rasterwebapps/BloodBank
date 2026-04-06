package com.bloodbank.donorservice.repository;

import com.bloodbank.donorservice.entity.CampResource;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CampResourceRepository extends JpaRepository<CampResource, UUID> {

    List<CampResource> findByCampId(UUID campId);
}
