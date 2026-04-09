package com.bloodbank.complianceservice.entity;

import com.bloodbank.complianceservice.enums.SopCategoryEnum;
import com.bloodbank.complianceservice.enums.SopStatusEnum;
import com.bloodbank.common.model.BranchScopedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "sop_documents")
@FilterDef(name = "branchFilter", parameters = @ParamDef(name = "branchId", type = UUID.class))
@Filter(name = "branchFilter", condition = "branch_id = :branchId")
public class SopDocument extends BranchScopedEntity {

    @Column(name = "sop_code", nullable = false, unique = true, length = 50)
    private String sopCode;

    @Column(name = "sop_title", nullable = false, length = 200)
    private String sopTitle;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private SopCategoryEnum category;

    @Column(name = "framework_id")
    private UUID frameworkId;

    @Column(name = "version_number", nullable = false, length = 20)
    private String versionNumber = "1.0";

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Column(name = "review_date")
    private LocalDate reviewDate;

    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "document_url", length = 500)
    private String documentUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SopStatusEnum status = SopStatusEnum.DRAFT;

    public SopDocument() {}

    public String getSopCode() { return sopCode; }
    public void setSopCode(String sopCode) { this.sopCode = sopCode; }

    public String getSopTitle() { return sopTitle; }
    public void setSopTitle(String sopTitle) { this.sopTitle = sopTitle; }

    public SopCategoryEnum getCategory() { return category; }
    public void setCategory(SopCategoryEnum category) { this.category = category; }

    public UUID getFrameworkId() { return frameworkId; }
    public void setFrameworkId(UUID frameworkId) { this.frameworkId = frameworkId; }

    public String getVersionNumber() { return versionNumber; }
    public void setVersionNumber(String versionNumber) { this.versionNumber = versionNumber; }

    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }

    public LocalDate getReviewDate() { return reviewDate; }
    public void setReviewDate(LocalDate reviewDate) { this.reviewDate = reviewDate; }

    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }

    public Instant getApprovedAt() { return approvedAt; }
    public void setApprovedAt(Instant approvedAt) { this.approvedAt = approvedAt; }

    public String getDocumentUrl() { return documentUrl; }
    public void setDocumentUrl(String documentUrl) { this.documentUrl = documentUrl; }

    public SopStatusEnum getStatus() { return status; }
    public void setStatus(SopStatusEnum status) { this.status = status; }
}
