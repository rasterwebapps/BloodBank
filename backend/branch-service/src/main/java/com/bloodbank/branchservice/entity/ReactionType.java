package com.bloodbank.branchservice.entity;

import com.bloodbank.common.model.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "reaction_types")
public class ReactionType extends BaseEntity {

    @Column(name = "reaction_code", nullable = false, unique = true, length = 50)
    private String reactionCode;

    @Column(name = "reaction_name", nullable = false, length = 100)
    private String reactionName;

    @Column(name = "severity", nullable = false, length = 20)
    private String severity;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    protected ReactionType() {}

    public ReactionType(String reactionCode, String reactionName, String severity) {
        this.reactionCode = reactionCode;
        this.reactionName = reactionName;
        this.severity = severity;
    }

    public String getReactionCode() { return reactionCode; }
    public void setReactionCode(String reactionCode) { this.reactionCode = reactionCode; }

    public String getReactionName() { return reactionName; }
    public void setReactionName(String reactionName) { this.reactionName = reactionName; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
