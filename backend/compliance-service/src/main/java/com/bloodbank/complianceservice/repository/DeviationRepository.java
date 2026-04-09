package com.bloodbank.complianceservice.repository;

import com.bloodbank.complianceservice.entity.Deviation;
import com.bloodbank.complianceservice.enums.DeviationSeverityEnum;
import com.bloodbank.complianceservice.enums.DeviationStatusEnum;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeviationRepository extends JpaRepository<Deviation, UUID>,
                                              JpaSpecificationExecutor<Deviation> {

    Optional<Deviation> findByDeviationNumber(String deviationNumber);

    List<Deviation> findByStatus(DeviationStatusEnum status);

    List<Deviation> findBySeverity(DeviationSeverityEnum severity);
}
