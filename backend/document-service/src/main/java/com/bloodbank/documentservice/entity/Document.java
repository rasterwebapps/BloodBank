package com.bloodbank.documentservice.entity;

import com.bloodbank.common.model.BaseEntity;
import com.bloodbank.documentservice.enums.DocumentStatusEnum;
import com.bloodbank.documentservice.enums.DocumentTypeEnum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "documents")
public class Document extends BaseEntity {

    @Column(name = "branch_id")
    private UUID branchId;

    @Column(name = "document_code", nullable = false, unique = true, length = 50)
    private String documentCode;

    @Column(name = "document_name", nullable = false, length = 200)
    private String documentName;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 50)
    private DocumentTypeEnum documentType;

    @Column(name = "entity_type", length = 100)
    private String entityType;

    @Column(name = "entity_id")
    private UUID entityId;

    @Column(name = "mime_type", length = 100)
    private String mimeType;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Column(name = "storage_path", length = 500)
    private String storagePath;

    @Column(name = "storage_bucket", length = 100)
    private String storageBucket = "bloodbank-documents";

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "tags", length = 500)
    private String tags;

    @Column(name = "is_confidential")
    private boolean confidential;

    @Column(name = "uploaded_by", length = 255)
    private String uploadedBy;

    @Column(name = "current_version")
    private int currentVersion = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private DocumentStatusEnum status = DocumentStatusEnum.ACTIVE;

    protected Document() {}

    public Document(String documentName, DocumentTypeEnum documentType) {
        this.documentName = documentName;
        this.documentType = documentType;
        this.status = DocumentStatusEnum.ACTIVE;
        this.currentVersion = 1;
    }

    public UUID getBranchId() { return branchId; }
    public void setBranchId(UUID branchId) { this.branchId = branchId; }

    public String getDocumentCode() { return documentCode; }
    public void setDocumentCode(String documentCode) { this.documentCode = documentCode; }

    public String getDocumentName() { return documentName; }
    public void setDocumentName(String documentName) { this.documentName = documentName; }

    public DocumentTypeEnum getDocumentType() { return documentType; }
    public void setDocumentType(DocumentTypeEnum documentType) { this.documentType = documentType; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public UUID getEntityId() { return entityId; }
    public void setEntityId(UUID entityId) { this.entityId = entityId; }

    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }

    public Long getFileSizeBytes() { return fileSizeBytes; }
    public void setFileSizeBytes(Long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; }

    public String getStoragePath() { return storagePath; }
    public void setStoragePath(String storagePath) { this.storagePath = storagePath; }

    public String getStorageBucket() { return storageBucket; }
    public void setStorageBucket(String storageBucket) { this.storageBucket = storageBucket; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public boolean isConfidential() { return confidential; }
    public void setConfidential(boolean confidential) { this.confidential = confidential; }

    public String getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(String uploadedBy) { this.uploadedBy = uploadedBy; }

    public int getCurrentVersion() { return currentVersion; }
    public void setCurrentVersion(int currentVersion) { this.currentVersion = currentVersion; }

    public DocumentStatusEnum getStatus() { return status; }
    public void setStatus(DocumentStatusEnum status) { this.status = status; }
}
