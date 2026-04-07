package com.bloodbank.reportingservice.entity;

import com.bloodbank.common.model.BaseEntity;
import com.bloodbank.reportingservice.enums.WidgetTypeEnum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "dashboard_widgets")
public class DashboardWidget extends BaseEntity {

    @Column(name = "branch_id")
    private UUID branchId;

    @Column(name = "widget_code", nullable = false, length = 50)
    private String widgetCode;

    @Column(name = "widget_name", nullable = false, length = 200)
    private String widgetName;

    @Enumerated(EnumType.STRING)
    @Column(name = "widget_type", nullable = false, length = 30)
    private WidgetTypeEnum widgetType;

    @Column(name = "data_source", length = 100)
    private String dataSource;

    @Column(name = "query_definition", columnDefinition = "TEXT")
    private String queryDefinition;

    @Column(name = "display_config", columnDefinition = "TEXT")
    private String displayConfig;

    @Column(name = "refresh_interval_sec", nullable = false)
    private int refreshIntervalSec = 300;

    @Column(name = "role_access", length = 500)
    private String roleAccess;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    protected DashboardWidget() {}

    public DashboardWidget(String widgetCode, String widgetName, WidgetTypeEnum widgetType) {
        this.widgetCode = widgetCode;
        this.widgetName = widgetName;
        this.widgetType = widgetType;
    }

    public UUID getBranchId() { return branchId; }
    public void setBranchId(UUID branchId) { this.branchId = branchId; }

    public String getWidgetCode() { return widgetCode; }
    public void setWidgetCode(String widgetCode) { this.widgetCode = widgetCode; }

    public String getWidgetName() { return widgetName; }
    public void setWidgetName(String widgetName) { this.widgetName = widgetName; }

    public WidgetTypeEnum getWidgetType() { return widgetType; }
    public void setWidgetType(WidgetTypeEnum widgetType) { this.widgetType = widgetType; }

    public String getDataSource() { return dataSource; }
    public void setDataSource(String dataSource) { this.dataSource = dataSource; }

    public String getQueryDefinition() { return queryDefinition; }
    public void setQueryDefinition(String queryDefinition) { this.queryDefinition = queryDefinition; }

    public String getDisplayConfig() { return displayConfig; }
    public void setDisplayConfig(String displayConfig) { this.displayConfig = displayConfig; }

    public int getRefreshIntervalSec() { return refreshIntervalSec; }
    public void setRefreshIntervalSec(int refreshIntervalSec) { this.refreshIntervalSec = refreshIntervalSec; }

    public String getRoleAccess() { return roleAccess; }
    public void setRoleAccess(String roleAccess) { this.roleAccess = roleAccess; }

    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
