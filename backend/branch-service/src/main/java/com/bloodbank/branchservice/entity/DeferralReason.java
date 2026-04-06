package com.bloodbank.branchservice.entity;

import com.bloodbank.common.model.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "deferral_reasons")
public class DeferralReason extends BaseEntity {

    @Column(name = "reason_code", nullable = false, unique = true, length = 50)
    private String reasonCode;

    @Column(name = "reason_description", nullable = false, length = 500)
    private String reasonDescription;

    @Column(name = "deferral_type", nullable = false, length = 20)
    private String deferralType;

    @Column(name = "default_days")
    private Integer defaultDays;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    protected DeferralReason() {}

    public DeferralReason(String reasonCode, String reasonDescription, String deferralType) {
        this.reasonCode = reasonCode;
        this.reasonDescription = reasonDescription;
        this.deferralType = deferralType;
    }

    public String getReasonCode() { return reasonCode; }
    public void setReasonCode(String reasonCode) { this.reasonCode = reasonCode; }

    public String getReasonDescription() { return reasonDescription; }
    public void setReasonDescription(String reasonDescription) { this.reasonDescription = reasonDescription; }

    public String getDeferralType() { return deferralType; }
    public void setDeferralType(String deferralType) { this.deferralType = deferralType; }

    public Integer getDefaultDays() { return defaultDays; }
    public void setDefaultDays(Integer defaultDays) { this.defaultDays = defaultDays; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
