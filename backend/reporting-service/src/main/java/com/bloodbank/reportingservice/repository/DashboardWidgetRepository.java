package com.bloodbank.reportingservice.repository;

import com.bloodbank.reportingservice.entity.DashboardWidget;
import com.bloodbank.reportingservice.enums.WidgetTypeEnum;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.QueryHint;

public interface DashboardWidgetRepository extends JpaRepository<DashboardWidget, UUID>,
                                                   JpaSpecificationExecutor<DashboardWidget> {

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    Optional<DashboardWidget> findByWidgetCode(String widgetCode);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<DashboardWidget> findByActiveTrueOrderBySortOrderAsc();

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<DashboardWidget> findByWidgetTypeAndActiveTrue(WidgetTypeEnum widgetType);

    boolean existsByWidgetCode(String widgetCode);
}
