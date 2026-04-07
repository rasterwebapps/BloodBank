package com.bloodbank.reportingservice.entity;

import com.bloodbank.common.model.BaseEntity;
import com.bloodbank.reportingservice.enums.SignatureMeaningEnum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "digital_signatures")
public class DigitalSignature extends BaseEntity {

    @Column(name = "branch_id")
    private UUID branchId;

    @Column(name = "entity_type", nullable = false, length = 100)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    @Column(name = "signer_id", nullable = false)
    private String signerId;

    @Column(name = "signer_name", nullable = false, length = 200)
    private String signerName;

    @Column(name = "signer_role", length = 50)
    private String signerRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "signature_meaning", nullable = false, length = 100)
    private SignatureMeaningEnum signatureMeaning;

    @Column(name = "signature_hash", nullable = false, length = 512)
    private String signatureHash;

    @Column(name = "signature_algorithm", length = 50)
    private String signatureAlgorithm = "SHA-256";

    @Column(name = "signed_at", nullable = false)
    private Instant signedAt;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "is_valid", nullable = false)
    private boolean valid = true;

    protected DigitalSignature() {}

    public DigitalSignature(String entityType, UUID entityId, String signerId,
                            String signerName, SignatureMeaningEnum signatureMeaning,
                            String signatureHash, Instant signedAt) {
        this.entityType = entityType;
        this.entityId = entityId;
        this.signerId = signerId;
        this.signerName = signerName;
        this.signatureMeaning = signatureMeaning;
        this.signatureHash = signatureHash;
        this.signedAt = signedAt;
    }

    public UUID getBranchId() { return branchId; }
    public void setBranchId(UUID branchId) { this.branchId = branchId; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public UUID getEntityId() { return entityId; }
    public void setEntityId(UUID entityId) { this.entityId = entityId; }

    public String getSignerId() { return signerId; }
    public void setSignerId(String signerId) { this.signerId = signerId; }

    public String getSignerName() { return signerName; }
    public void setSignerName(String signerName) { this.signerName = signerName; }

    public String getSignerRole() { return signerRole; }
    public void setSignerRole(String signerRole) { this.signerRole = signerRole; }

    public SignatureMeaningEnum getSignatureMeaning() { return signatureMeaning; }
    public void setSignatureMeaning(SignatureMeaningEnum signatureMeaning) { this.signatureMeaning = signatureMeaning; }

    public String getSignatureHash() { return signatureHash; }
    public void setSignatureHash(String signatureHash) { this.signatureHash = signatureHash; }

    public String getSignatureAlgorithm() { return signatureAlgorithm; }
    public void setSignatureAlgorithm(String signatureAlgorithm) { this.signatureAlgorithm = signatureAlgorithm; }

    public Instant getSignedAt() { return signedAt; }
    public void setSignedAt(Instant signedAt) { this.signedAt = signedAt; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }
}
