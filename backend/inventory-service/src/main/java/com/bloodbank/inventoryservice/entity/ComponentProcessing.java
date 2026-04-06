package com.bloodbank.inventoryservice.entity;

import com.bloodbank.common.model.BranchScopedEntity;
import com.bloodbank.inventoryservice.enums.ProcessResultEnum;
import com.bloodbank.inventoryservice.enums.ProcessTypeEnum;
import jakarta.persistence.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "component_processing")
@FilterDef(name = "branchFilter", parameters = @ParamDef(name = "branchId", type = UUID.class))
@Filter(name = "branchFilter", condition = "branch_id = :branchId")
public class ComponentProcessing extends BranchScopedEntity {

    @Column(name = "component_id", nullable = false)
    private UUID componentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "process_type", nullable = false)
    private ProcessTypeEnum processType;

    @Column(name = "process_date")
    private Instant processDate;

    @Column(name = "processed_by")
    private String processedBy;

    @Column(name = "equipment_used")
    private String equipmentUsed;

    @Column(name = "parameters", columnDefinition = "TEXT")
    private String parameters;

    @Enumerated(EnumType.STRING)
    @Column(name = "result")
    private ProcessResultEnum result;

    @Column(name = "notes")
    private String notes;

    protected ComponentProcessing() {}

    public ComponentProcessing(UUID componentId, ProcessTypeEnum processType, String processedBy) {
        this.componentId = componentId;
        this.processType = processType;
        this.processedBy = processedBy;
        this.processDate = Instant.now();
    }

    public UUID getComponentId() { return componentId; }
    public void setComponentId(UUID componentId) { this.componentId = componentId; }

    public ProcessTypeEnum getProcessType() { return processType; }
    public void setProcessType(ProcessTypeEnum processType) { this.processType = processType; }

    public Instant getProcessDate() { return processDate; }
    public void setProcessDate(Instant processDate) { this.processDate = processDate; }

    public String getProcessedBy() { return processedBy; }
    public void setProcessedBy(String processedBy) { this.processedBy = processedBy; }

    public String getEquipmentUsed() { return equipmentUsed; }
    public void setEquipmentUsed(String equipmentUsed) { this.equipmentUsed = equipmentUsed; }

    public String getParameters() { return parameters; }
    public void setParameters(String parameters) { this.parameters = parameters; }

    public ProcessResultEnum getResult() { return result; }
    public void setResult(ProcessResultEnum result) { this.result = result; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
