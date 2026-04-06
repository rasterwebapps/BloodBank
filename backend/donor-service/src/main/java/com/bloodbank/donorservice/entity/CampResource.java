package com.bloodbank.donorservice.entity;

import com.bloodbank.common.model.BranchScopedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.util.UUID;

@Entity
@Table(name = "camp_resources")
@FilterDef(name = "branchFilter", parameters = @ParamDef(name = "branchId", type = UUID.class))
@Filter(name = "branchFilter", condition = "branch_id = :branchId")
public class CampResource extends BranchScopedEntity {

    @Column(name = "camp_id", nullable = false)
    private UUID campId;

    @Column(name = "resource_type", nullable = false, length = 30)
    private String resourceType;

    @Column(name = "resource_name", nullable = false, length = 200)
    private String resourceName;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    protected CampResource() {}

    public UUID getCampId() { return campId; }
    public void setCampId(UUID campId) { this.campId = campId; }

    public String getResourceType() { return resourceType; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }

    public String getResourceName() { return resourceName; }
    public void setResourceName(String resourceName) { this.resourceName = resourceName; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
