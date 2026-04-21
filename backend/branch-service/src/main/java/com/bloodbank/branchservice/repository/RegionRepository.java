package com.bloodbank.branchservice.repository;

import com.bloodbank.branchservice.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.QueryHint;

@Repository
public interface RegionRepository extends JpaRepository<Region, UUID> {
    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<Region> findByCountryId(UUID countryId);
    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<Region> findByIsActiveTrue();
    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    Optional<Region> findByCountryIdAndRegionCode(UUID countryId, String regionCode);
    boolean existsByCountryIdAndRegionCode(UUID countryId, String regionCode);
}
