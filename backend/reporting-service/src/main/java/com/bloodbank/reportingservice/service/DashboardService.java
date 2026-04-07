package com.bloodbank.reportingservice.service;

import com.bloodbank.common.exceptions.ResourceNotFoundException;
import com.bloodbank.reportingservice.dto.DashboardWidgetCreateRequest;
import com.bloodbank.reportingservice.dto.DashboardWidgetResponse;
import com.bloodbank.reportingservice.entity.DashboardWidget;
import com.bloodbank.reportingservice.mapper.DashboardWidgetMapper;
import com.bloodbank.reportingservice.repository.DashboardWidgetRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private static final Logger log = LoggerFactory.getLogger(DashboardService.class);

    private final DashboardWidgetRepository dashboardWidgetRepository;
    private final DashboardWidgetMapper dashboardWidgetMapper;

    public DashboardService(DashboardWidgetRepository dashboardWidgetRepository,
                            DashboardWidgetMapper dashboardWidgetMapper) {
        this.dashboardWidgetRepository = dashboardWidgetRepository;
        this.dashboardWidgetMapper = dashboardWidgetMapper;
    }

    @Transactional
    public DashboardWidgetResponse create(DashboardWidgetCreateRequest request) {
        log.info("Creating dashboard widget: {}", request.widgetCode());
        DashboardWidget widget = dashboardWidgetMapper.toEntity(request);
        widget = dashboardWidgetRepository.save(widget);
        return dashboardWidgetMapper.toResponse(widget);
    }

    public DashboardWidgetResponse getById(UUID id) {
        log.debug("Fetching dashboard widget by id {}", id);
        DashboardWidget widget = dashboardWidgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DashboardWidget", "id", id.toString()));
        return dashboardWidgetMapper.toResponse(widget);
    }

    public DashboardWidgetResponse getByCode(String widgetCode) {
        log.debug("Fetching dashboard widget by code {}", widgetCode);
        DashboardWidget widget = dashboardWidgetRepository.findByWidgetCode(widgetCode)
                .orElseThrow(() -> new ResourceNotFoundException("DashboardWidget", "widgetCode", widgetCode));
        return dashboardWidgetMapper.toResponse(widget);
    }

    public List<DashboardWidgetResponse> getActiveWidgets() {
        log.debug("Fetching all active dashboard widgets");
        List<DashboardWidget> widgets = dashboardWidgetRepository.findByActiveTrueOrderBySortOrderAsc();
        return dashboardWidgetMapper.toResponseList(widgets);
    }

    @Transactional
    public DashboardWidgetResponse update(UUID id, DashboardWidgetCreateRequest request) {
        log.info("Updating dashboard widget {}", id);
        DashboardWidget widget = dashboardWidgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DashboardWidget", "id", id.toString()));
        dashboardWidgetMapper.updateEntity(request, widget);
        widget = dashboardWidgetRepository.save(widget);
        return dashboardWidgetMapper.toResponse(widget);
    }

    @Transactional
    public void delete(UUID id) {
        log.info("Deleting dashboard widget {}", id);
        DashboardWidget widget = dashboardWidgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DashboardWidget", "id", id.toString()));
        widget.setActive(false);
        dashboardWidgetRepository.save(widget);
    }
}
