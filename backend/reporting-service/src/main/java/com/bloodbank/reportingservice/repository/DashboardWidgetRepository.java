package com.bloodbank.reportingservice.repository;

import com.bloodbank.reportingservice.entity.DashboardWidget;
import com.bloodbank.reportingservice.enums.WidgetTypeEnum;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DashboardWidgetRepository extends JpaRepository<DashboardWidget, UUID>,
                                                   JpaSpecificationExecutor<DashboardWidget> {

    Optional<DashboardWidget> findByWidgetCode(String widgetCode);

    List<DashboardWidget> findByActiveTrueOrderBySortOrderAsc();

    List<DashboardWidget> findByWidgetTypeAndActiveTrue(WidgetTypeEnum widgetType);

    boolean existsByWidgetCode(String widgetCode);
}
