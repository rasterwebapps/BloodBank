package com.bloodbank.donorservice.repository;

import com.bloodbank.donorservice.entity.CampDonor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CampDonorRepository extends JpaRepository<CampDonor, UUID> {

    List<CampDonor> findByCampId(UUID campId);

    Optional<CampDonor> findByCampIdAndDonorId(UUID campId, UUID donorId);

    boolean existsByCampIdAndDonorId(UUID campId, UUID donorId);
}
