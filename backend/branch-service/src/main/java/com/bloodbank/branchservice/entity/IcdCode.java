package com.bloodbank.branchservice.entity;

import com.bloodbank.common.model.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "icd_codes")
public class IcdCode extends BaseEntity {

    @Column(name = "icd_code", nullable = false, unique = true, length = 20)
    private String icdCode;

    @Column(name = "description", nullable = false, length = 500)
    private String description;

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    protected IcdCode() {}

    public IcdCode(String icdCode, String description) {
        this.icdCode = icdCode;
        this.description = description;
    }

    public String getIcdCode() { return icdCode; }
    public void setIcdCode(String icdCode) { this.icdCode = icdCode; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
