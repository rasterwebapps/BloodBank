package com.bloodbank.donorservice.entity;

import com.bloodbank.common.model.BranchScopedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "donor_consents")
@FilterDef(name = "branchFilter", parameters = @ParamDef(name = "branchId", type = UUID.class))
@Filter(name = "branchFilter", condition = "branch_id = :branchId")
public class DonorConsent extends BranchScopedEntity {

    @Column(name = "donor_id", nullable = false)
    private UUID donorId;

    @Column(name = "consent_type", nullable = false, length = 50)
    private String consentType;

    @Column(name = "consent_given", nullable = false)
    private boolean consentGiven;

    @Column(name = "consent_date", nullable = false)
    private Instant consentDate;

    @Column(name = "expiry_date")
    private Instant expiryDate;

    @Column(name = "consent_text", columnDefinition = "TEXT")
    private String consentText;

    @Column(name = "signature_reference", length = 255)
    private String signatureReference;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    protected DonorConsent() {}

    public UUID getDonorId() { return donorId; }
    public void setDonorId(UUID donorId) { this.donorId = donorId; }

    public String getConsentType() { return consentType; }
    public void setConsentType(String consentType) { this.consentType = consentType; }

    public boolean isConsentGiven() { return consentGiven; }
    public void setConsentGiven(boolean consentGiven) { this.consentGiven = consentGiven; }

    public Instant getConsentDate() { return consentDate; }
    public void setConsentDate(Instant consentDate) { this.consentDate = consentDate; }

    public Instant getExpiryDate() { return expiryDate; }
    public void setExpiryDate(Instant expiryDate) { this.expiryDate = expiryDate; }

    public String getConsentText() { return consentText; }
    public void setConsentText(String consentText) { this.consentText = consentText; }

    public String getSignatureReference() { return signatureReference; }
    public void setSignatureReference(String signatureReference) { this.signatureReference = signatureReference; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public Instant getRevokedAt() { return revokedAt; }
    public void setRevokedAt(Instant revokedAt) { this.revokedAt = revokedAt; }
}
