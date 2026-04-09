package com.bloodbank.complianceservice.entity;

import com.bloodbank.common.model.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "regulatory_frameworks")
public class RegulatoryFramework extends BaseEntity {

    @Column(name = "framework_code", nullable = false, unique = true, length = 50)
    private String frameworkCode;

    @Column(name = "framework_name", nullable = false, length = 200)
    private String frameworkName;

    @Column(name = "authority_name", length = 200)
    private String authorityName;

    @Column(name = "country_id")
    private UUID countryId;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "effective_date")
    private LocalDate effectiveDate;

    @Column(name = "version_number", length = 20)
    private String versionNumber;

    @Column(name = "document_url", length = 500)
    private String documentUrl;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    public RegulatoryFramework() {}

    public String getFrameworkCode() { return frameworkCode; }
    public void setFrameworkCode(String frameworkCode) { this.frameworkCode = frameworkCode; }

    public String getFrameworkName() { return frameworkName; }
    public void setFrameworkName(String frameworkName) { this.frameworkName = frameworkName; }

    public String getAuthorityName() { return authorityName; }
    public void setAuthorityName(String authorityName) { this.authorityName = authorityName; }

    public UUID getCountryId() { return countryId; }
    public void setCountryId(UUID countryId) { this.countryId = countryId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }

    public String getVersionNumber() { return versionNumber; }
    public void setVersionNumber(String versionNumber) { this.versionNumber = versionNumber; }

    public String getDocumentUrl() { return documentUrl; }
    public void setDocumentUrl(String documentUrl) { this.documentUrl = documentUrl; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
