package com.bloodbank.billingservice.entity;

import com.bloodbank.common.model.BranchScopedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "rate_master")
@FilterDef(name = "branchFilter", parameters = @ParamDef(name = "branchId", type = UUID.class))
@Filter(name = "branchFilter", condition = "branch_id = :branchId")
public class RateMaster extends BranchScopedEntity {

    @Column(name = "component_type_id")
    private UUID componentTypeId;

    @Column(name = "service_code", length = 50)
    private String serviceCode;

    @Column(name = "service_name", length = 200)
    private String serviceName;

    @Column(name = "rate_amount", precision = 12, scale = 2)
    private BigDecimal rateAmount;

    @Column(name = "currency", length = 3)
    private String currency = "USD";

    @Column(name = "tax_percentage", precision = 5, scale = 2)
    private BigDecimal taxPercentage;

    @Column(name = "effective_from")
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(name = "is_active")
    private boolean active = true;

    protected RateMaster() {}

    public RateMaster(String serviceCode, String serviceName, BigDecimal rateAmount) {
        this.serviceCode = serviceCode;
        this.serviceName = serviceName;
        this.rateAmount = rateAmount;
    }

    public UUID getComponentTypeId() { return componentTypeId; }
    public void setComponentTypeId(UUID componentTypeId) { this.componentTypeId = componentTypeId; }

    public String getServiceCode() { return serviceCode; }
    public void setServiceCode(String serviceCode) { this.serviceCode = serviceCode; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public BigDecimal getRateAmount() { return rateAmount; }
    public void setRateAmount(BigDecimal rateAmount) { this.rateAmount = rateAmount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public BigDecimal getTaxPercentage() { return taxPercentage; }
    public void setTaxPercentage(BigDecimal taxPercentage) { this.taxPercentage = taxPercentage; }

    public LocalDate getEffectiveFrom() { return effectiveFrom; }
    public void setEffectiveFrom(LocalDate effectiveFrom) { this.effectiveFrom = effectiveFrom; }

    public LocalDate getEffectiveTo() { return effectiveTo; }
    public void setEffectiveTo(LocalDate effectiveTo) { this.effectiveTo = effectiveTo; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
