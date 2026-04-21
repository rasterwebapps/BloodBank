package com.bloodbank.branchservice.repository;

import com.bloodbank.branchservice.entity.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.QueryHint;

@Repository
public interface CityRepository extends JpaRepository<City, UUID> {
    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<City> findByRegionId(UUID regionId);
    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<City> findByIsActiveTrue();
}
