package com.bloodbank.branchservice.repository;

import com.bloodbank.branchservice.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RegionRepository extends JpaRepository<Region, UUID> {
    List<Region> findByCountryId(UUID countryId);
    List<Region> findByIsActiveTrue();
    Optional<Region> findByCountryIdAndRegionCode(UUID countryId, String regionCode);
    boolean existsByCountryIdAndRegionCode(UUID countryId, String regionCode);
}
