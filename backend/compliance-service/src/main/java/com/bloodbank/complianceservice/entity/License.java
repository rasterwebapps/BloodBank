package com.bloodbank.complianceservice.entity;

import com.bloodbank.complianceservice.enums.LicenseStatusEnum;
import com.bloodbank.complianceservice.enums.LicenseTypeEnum;
import com.bloodbank.common.model.BranchScopedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "licenses")
@FilterDef(name = "branchFilter", parameters = @ParamDef(name = "branchId", type = UUID.class))
@Filter(name = "branchFilter", condition = "branch_id = :branchId")
public class License extends BranchScopedEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "license_type", nullable = false, length = 50)
    private LicenseTypeEnum licenseType;

    @Column(name = "license_number", nullable = false, length = 100)
    private String licenseNumber;

    @Column(name = "issuing_authority", nullable = false, length = 200)
    private String issuingAuthority;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(name = "renewal_date")
    private LocalDate renewalDate;

    @Column(name = "scope", columnDefinition = "TEXT")
    private String scope;

    @Column(name = "document_url", length = 500)
    private String documentUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private LicenseStatusEnum status = LicenseStatusEnum.ACTIVE;

    public License() {}

    public LicenseTypeEnum getLicenseType() { return licenseType; }
    public void setLicenseType(LicenseTypeEnum licenseType) { this.licenseType = licenseType; }

    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }

    public String getIssuingAuthority() { return issuingAuthority; }
    public void setIssuingAuthority(String issuingAuthority) { this.issuingAuthority = issuingAuthority; }

    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public LocalDate getRenewalDate() { return renewalDate; }
    public void setRenewalDate(LocalDate renewalDate) { this.renewalDate = renewalDate; }

    public String getScope() { return scope; }
    public void setScope(String scope) { this.scope = scope; }

    public String getDocumentUrl() { return documentUrl; }
    public void setDocumentUrl(String documentUrl) { this.documentUrl = documentUrl; }

    public LicenseStatusEnum getStatus() { return status; }
    public void setStatus(LicenseStatusEnum status) { this.status = status; }
}
