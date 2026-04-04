package com.bloodbank.branchservice.repository;

import com.bloodbank.branchservice.entity.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CountryRepository extends JpaRepository<Country, UUID> {
    Optional<Country> findByCountryCode(String countryCode);
    List<Country> findByIsActiveTrue();
    boolean existsByCountryCode(String countryCode);
}
