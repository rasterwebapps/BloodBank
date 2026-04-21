package com.bloodbank.complianceservice.repository;

import com.bloodbank.complianceservice.entity.License;
import com.bloodbank.complianceservice.enums.LicenseStatusEnum;
import com.bloodbank.complianceservice.enums.LicenseTypeEnum;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.QueryHint;

@Repository
public interface LicenseRepository extends JpaRepository<License, UUID>,
                                            JpaSpecificationExecutor<License> {

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    Optional<License> findByLicenseNumber(String licenseNumber);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<License> findByStatus(LicenseStatusEnum status);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<License> findByLicenseType(LicenseTypeEnum licenseType);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<License> findByExpiryDateBefore(LocalDate date);
}
