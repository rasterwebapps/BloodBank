package com.bloodbank.branchservice.entity;

import com.bloodbank.common.model.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "blood_groups")
public class BloodGroup extends BaseEntity {

    @Column(name = "group_name", nullable = false, unique = true, length = 10)
    private String groupName;

    @Column(name = "description")
    private String description;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    protected BloodGroup() {}

    public BloodGroup(String groupName, String description) {
        this.groupName = groupName;
        this.description = description;
    }

    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
