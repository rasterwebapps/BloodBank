package com.bloodbank.labservice.entity;

import com.bloodbank.common.model.BranchScopedEntity;
import com.bloodbank.labservice.enums.OrderPriorityEnum;
import com.bloodbank.labservice.enums.OrderStatusEnum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "test_orders")
@FilterDef(name = "branchFilter", parameters = @ParamDef(name = "branchId", type = UUID.class))
@Filter(name = "branchFilter", condition = "branch_id = :branchId")
public class TestOrder extends BranchScopedEntity {

    @Column(name = "sample_id", nullable = false)
    private UUID sampleId;

    @Column(name = "collection_id", nullable = false)
    private UUID collectionId;

    @Column(name = "donor_id", nullable = false)
    private UUID donorId;

    @Column(name = "panel_id")
    private UUID panelId;

    @Column(name = "order_number", nullable = false, unique = true, length = 30)
    private String orderNumber;

    @Column(name = "order_date", nullable = false)
    private Instant orderDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    private OrderPriorityEnum priority;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OrderStatusEnum status = OrderStatusEnum.PENDING;

    @Column(name = "ordered_by")
    private String orderedBy;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    protected TestOrder() {}

    public TestOrder(UUID sampleId, UUID collectionId, UUID donorId,
                     OrderPriorityEnum priority) {
        this.sampleId = sampleId;
        this.collectionId = collectionId;
        this.donorId = donorId;
        this.priority = priority;
        this.status = OrderStatusEnum.PENDING;
        this.orderDate = Instant.now();
    }

    public UUID getSampleId() { return sampleId; }
    public void setSampleId(UUID sampleId) { this.sampleId = sampleId; }

    public UUID getCollectionId() { return collectionId; }
    public void setCollectionId(UUID collectionId) { this.collectionId = collectionId; }

    public UUID getDonorId() { return donorId; }
    public void setDonorId(UUID donorId) { this.donorId = donorId; }

    public UUID getPanelId() { return panelId; }
    public void setPanelId(UUID panelId) { this.panelId = panelId; }

    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

    public Instant getOrderDate() { return orderDate; }
    public void setOrderDate(Instant orderDate) { this.orderDate = orderDate; }

    public OrderPriorityEnum getPriority() { return priority; }
    public void setPriority(OrderPriorityEnum priority) { this.priority = priority; }

    public OrderStatusEnum getStatus() { return status; }
    public void setStatus(OrderStatusEnum status) { this.status = status; }

    public String getOrderedBy() { return orderedBy; }
    public void setOrderedBy(String orderedBy) { this.orderedBy = orderedBy; }

    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
