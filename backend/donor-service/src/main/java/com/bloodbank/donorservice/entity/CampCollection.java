package com.bloodbank.donorservice.entity;

import com.bloodbank.common.model.BranchScopedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.util.UUID;

@Entity
@Table(name = "camp_collections", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"camp_id", "collection_id"})
})
@FilterDef(name = "branchFilter", parameters = @ParamDef(name = "branchId", type = UUID.class))
@Filter(name = "branchFilter", condition = "branch_id = :branchId")
public class CampCollection extends BranchScopedEntity {

    @Column(name = "camp_id", nullable = false)
    private UUID campId;

    @Column(name = "collection_id", nullable = false)
    private UUID collectionId;

    protected CampCollection() {}

    public UUID getCampId() { return campId; }
    public void setCampId(UUID campId) { this.campId = campId; }

    public UUID getCollectionId() { return collectionId; }
    public void setCollectionId(UUID collectionId) { this.collectionId = collectionId; }
}
