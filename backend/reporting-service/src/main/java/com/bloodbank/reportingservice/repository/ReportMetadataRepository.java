package com.bloodbank.reportingservice.repository;

import com.bloodbank.reportingservice.entity.ReportMetadata;
import com.bloodbank.reportingservice.enums.ReportTypeEnum;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.QueryHint;

public interface ReportMetadataRepository extends JpaRepository<ReportMetadata, UUID>,
                                                  JpaSpecificationExecutor<ReportMetadata> {

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    Optional<ReportMetadata> findByReportCode(String reportCode);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<ReportMetadata> findByReportTypeAndActiveTrue(ReportTypeEnum reportType);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<ReportMetadata> findByActiveTrue();

    boolean existsByReportCode(String reportCode);
}
