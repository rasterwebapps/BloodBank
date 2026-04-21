package com.bloodbank.branchservice.repository;

import com.bloodbank.branchservice.entity.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.QueryHint;

@Repository
public interface CountryRepository extends JpaRepository<Country, UUID> {
    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    Optional<Country> findByCountryCode(String countryCode);
    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<Country> findByIsActiveTrue();
    boolean existsByCountryCode(String countryCode);
}
