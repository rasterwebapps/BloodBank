package com.bloodbank.reportingservice.repository;

import com.bloodbank.reportingservice.entity.ReportMetadata;
import com.bloodbank.reportingservice.enums.ReportTypeEnum;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReportMetadataRepository extends JpaRepository<ReportMetadata, UUID>,
                                                  JpaSpecificationExecutor<ReportMetadata> {

    Optional<ReportMetadata> findByReportCode(String reportCode);

    List<ReportMetadata> findByReportTypeAndActiveTrue(ReportTypeEnum reportType);

    List<ReportMetadata> findByActiveTrue();

    boolean existsByReportCode(String reportCode);
}
