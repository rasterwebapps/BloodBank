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

@Repository
public interface LicenseRepository extends JpaRepository<License, UUID>,
                                            JpaSpecificationExecutor<License> {

    Optional<License> findByLicenseNumber(String licenseNumber);

    List<License> findByStatus(LicenseStatusEnum status);

    List<License> findByLicenseType(LicenseTypeEnum licenseType);

    List<License> findByExpiryDateBefore(LocalDate date);
}
