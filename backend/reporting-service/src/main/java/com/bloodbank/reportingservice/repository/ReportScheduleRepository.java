package com.bloodbank.reportingservice.repository;

import com.bloodbank.reportingservice.entity.ReportSchedule;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface ReportScheduleRepository extends JpaRepository<ReportSchedule, UUID>,
                                                  JpaSpecificationExecutor<ReportSchedule> {

    List<ReportSchedule> findByReportIdOrderByCreatedAtDesc(UUID reportId);

    List<ReportSchedule> findByActiveTrue();
}
