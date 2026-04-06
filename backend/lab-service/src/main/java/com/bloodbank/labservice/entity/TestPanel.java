package com.bloodbank.labservice.entity;

import com.bloodbank.common.model.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "test_panels")
public class TestPanel extends BaseEntity {

    @Column(name = "panel_code", nullable = false, unique = true, length = 50)
    private String panelCode;

    @Column(name = "panel_name", nullable = false, length = 200)
    private String panelName;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "test_names", columnDefinition = "TEXT")
    private String testNames;

    @Column(name = "is_mandatory", nullable = false)
    private boolean isMandatory;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    protected TestPanel() {}

    public TestPanel(String panelCode, String panelName, String description,
                     String testNames, boolean isMandatory) {
        this.panelCode = panelCode;
        this.panelName = panelName;
        this.description = description;
        this.testNames = testNames;
        this.isMandatory = isMandatory;
        this.isActive = true;
    }

    public String getPanelCode() { return panelCode; }
    public void setPanelCode(String panelCode) { this.panelCode = panelCode; }

    public String getPanelName() { return panelName; }
    public void setPanelName(String panelName) { this.panelName = panelName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTestNames() { return testNames; }
    public void setTestNames(String testNames) { this.testNames = testNames; }

    public boolean isMandatory() { return isMandatory; }
    public void setMandatory(boolean mandatory) { this.isMandatory = mandatory; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { this.isActive = active; }
}
