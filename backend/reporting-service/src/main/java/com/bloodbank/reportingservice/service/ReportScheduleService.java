package com.bloodbank.reportingservice.service;

import com.bloodbank.common.exceptions.ResourceNotFoundException;
import com.bloodbank.reportingservice.dto.ReportScheduleCreateRequest;
import com.bloodbank.reportingservice.dto.ReportScheduleResponse;
import com.bloodbank.reportingservice.entity.ReportSchedule;
import com.bloodbank.reportingservice.mapper.ReportScheduleMapper;
import com.bloodbank.reportingservice.repository.ReportScheduleRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ReportScheduleService {

    private static final Logger log = LoggerFactory.getLogger(ReportScheduleService.class);

    private final ReportScheduleRepository reportScheduleRepository;
    private final ReportScheduleMapper reportScheduleMapper;

    public ReportScheduleService(ReportScheduleRepository reportScheduleRepository,
                                 ReportScheduleMapper reportScheduleMapper) {
        this.reportScheduleRepository = reportScheduleRepository;
        this.reportScheduleMapper = reportScheduleMapper;
    }

    @Transactional
    public ReportScheduleResponse create(ReportScheduleCreateRequest request) {
        log.info("Creating report schedule: {}", request.scheduleName());
        ReportSchedule schedule = reportScheduleMapper.toEntity(request);
        schedule = reportScheduleRepository.save(schedule);
        return reportScheduleMapper.toResponse(schedule);
    }

    public ReportScheduleResponse getById(UUID id) {
        log.debug("Fetching report schedule by id {}", id);
        ReportSchedule schedule = reportScheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ReportSchedule", "id", id.toString()));
        return reportScheduleMapper.toResponse(schedule);
    }

    public List<ReportScheduleResponse> getByReportId(UUID reportId) {
        log.debug("Fetching schedules for report {}", reportId);
        List<ReportSchedule> schedules = reportScheduleRepository.findByReportIdOrderByCreatedAtDesc(reportId);
        return reportScheduleMapper.toResponseList(schedules);
    }

    public List<ReportScheduleResponse> getActiveSchedules() {
        log.debug("Fetching all active schedules");
        List<ReportSchedule> schedules = reportScheduleRepository.findByActiveTrue();
        return reportScheduleMapper.toResponseList(schedules);
    }

    @Transactional
    public ReportScheduleResponse update(UUID id, ReportScheduleCreateRequest request) {
        log.info("Updating report schedule {}", id);
        ReportSchedule schedule = reportScheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ReportSchedule", "id", id.toString()));
        reportScheduleMapper.updateEntity(request, schedule);
        schedule = reportScheduleRepository.save(schedule);
        return reportScheduleMapper.toResponse(schedule);
    }

    @Transactional
    public void delete(UUID id) {
        log.info("Deleting report schedule {}", id);
        ReportSchedule schedule = reportScheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ReportSchedule", "id", id.toString()));
        schedule.setActive(false);
        reportScheduleRepository.save(schedule);
    }
}
