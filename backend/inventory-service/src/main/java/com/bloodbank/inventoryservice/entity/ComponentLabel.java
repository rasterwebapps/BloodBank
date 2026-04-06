package com.bloodbank.inventoryservice.entity;

import com.bloodbank.common.model.BranchScopedEntity;
import com.bloodbank.inventoryservice.enums.LabelTypeEnum;
import jakarta.persistence.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "component_labels")
@FilterDef(name = "branchFilter", parameters = @ParamDef(name = "branchId", type = UUID.class))
@Filter(name = "branchFilter", condition = "branch_id = :branchId")
public class ComponentLabel extends BranchScopedEntity {

    @Column(name = "component_id", nullable = false)
    private UUID componentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "label_type", nullable = false)
    private LabelTypeEnum labelType;

    @Column(name = "label_data", columnDefinition = "TEXT", nullable = false)
    private String labelData;

    @Column(name = "printed_at")
    private Instant printedAt;

    @Column(name = "printed_by")
    private String printedBy;

    @Column(name = "reprint_count")
    private int reprintCount;

    protected ComponentLabel() {}

    public ComponentLabel(UUID componentId, LabelTypeEnum labelType, String labelData, String printedBy) {
        this.componentId = componentId;
        this.labelType = labelType;
        this.labelData = labelData;
        this.printedBy = printedBy;
        this.printedAt = Instant.now();
        this.reprintCount = 0;
    }

    public UUID getComponentId() { return componentId; }
    public void setComponentId(UUID componentId) { this.componentId = componentId; }

    public LabelTypeEnum getLabelType() { return labelType; }
    public void setLabelType(LabelTypeEnum labelType) { this.labelType = labelType; }

    public String getLabelData() { return labelData; }
    public void setLabelData(String labelData) { this.labelData = labelData; }

    public Instant getPrintedAt() { return printedAt; }
    public void setPrintedAt(Instant printedAt) { this.printedAt = printedAt; }

    public String getPrintedBy() { return printedBy; }
    public void setPrintedBy(String printedBy) { this.printedBy = printedBy; }

    public int getReprintCount() { return reprintCount; }
    public void setReprintCount(int reprintCount) { this.reprintCount = reprintCount; }
}
