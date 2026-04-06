package com.bloodbank.donorservice.entity;

import com.bloodbank.common.model.BranchScopedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "donor_health_records")
@FilterDef(name = "branchFilter", parameters = @ParamDef(name = "branchId", type = UUID.class))
@Filter(name = "branchFilter", condition = "branch_id = :branchId")
public class DonorHealthRecord extends BranchScopedEntity {

    @Column(name = "donor_id", nullable = false)
    private UUID donorId;

    @Column(name = "screening_date", nullable = false)
    private Instant screeningDate;

    @Column(name = "weight_kg", precision = 5, scale = 2)
    private BigDecimal weightKg;

    @Column(name = "height_cm", precision = 5, scale = 2)
    private BigDecimal heightCm;

    @Column(name = "blood_pressure_systolic")
    private Integer bloodPressureSystolic;

    @Column(name = "blood_pressure_diastolic")
    private Integer bloodPressureDiastolic;

    @Column(name = "pulse_rate")
    private Integer pulseRate;

    @Column(name = "temperature_celsius", precision = 4, scale = 2)
    private BigDecimal temperatureCelsius;

    @Column(name = "hemoglobin_gdl", precision = 4, scale = 2)
    private BigDecimal hemoglobinGdl;

    @Column(name = "is_eligible", nullable = false)
    private boolean eligible;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "screened_by", length = 255)
    private String screenedBy;

    public DonorHealthRecord() {}

    public UUID getDonorId() { return donorId; }
    public void setDonorId(UUID donorId) { this.donorId = donorId; }

    public Instant getScreeningDate() { return screeningDate; }
    public void setScreeningDate(Instant screeningDate) { this.screeningDate = screeningDate; }

    public BigDecimal getWeightKg() { return weightKg; }
    public void setWeightKg(BigDecimal weightKg) { this.weightKg = weightKg; }

    public BigDecimal getHeightCm() { return heightCm; }
    public void setHeightCm(BigDecimal heightCm) { this.heightCm = heightCm; }

    public Integer getBloodPressureSystolic() { return bloodPressureSystolic; }
    public void setBloodPressureSystolic(Integer bloodPressureSystolic) { this.bloodPressureSystolic = bloodPressureSystolic; }

    public Integer getBloodPressureDiastolic() { return bloodPressureDiastolic; }
    public void setBloodPressureDiastolic(Integer bloodPressureDiastolic) { this.bloodPressureDiastolic = bloodPressureDiastolic; }

    public Integer getPulseRate() { return pulseRate; }
    public void setPulseRate(Integer pulseRate) { this.pulseRate = pulseRate; }

    public BigDecimal getTemperatureCelsius() { return temperatureCelsius; }
    public void setTemperatureCelsius(BigDecimal temperatureCelsius) { this.temperatureCelsius = temperatureCelsius; }

    public BigDecimal getHemoglobinGdl() { return hemoglobinGdl; }
    public void setHemoglobinGdl(BigDecimal hemoglobinGdl) { this.hemoglobinGdl = hemoglobinGdl; }

    public boolean isEligible() { return eligible; }
    public void setEligible(boolean eligible) { this.eligible = eligible; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getScreenedBy() { return screenedBy; }
    public void setScreenedBy(String screenedBy) { this.screenedBy = screenedBy; }
}
