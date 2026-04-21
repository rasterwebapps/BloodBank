package com.bloodbank.reportingservice.repository;

import com.bloodbank.reportingservice.entity.ReportSchedule;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.QueryHint;

public interface ReportScheduleRepository extends JpaRepository<ReportSchedule, UUID>,
                                                  JpaSpecificationExecutor<ReportSchedule> {

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<ReportSchedule> findByReportIdOrderByCreatedAtDesc(UUID reportId);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<ReportSchedule> findByActiveTrue();
}
