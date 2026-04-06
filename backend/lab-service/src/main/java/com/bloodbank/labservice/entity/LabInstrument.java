package com.bloodbank.labservice.entity;

import com.bloodbank.common.model.BranchScopedEntity;
import com.bloodbank.labservice.enums.InstrumentStatusEnum;
import com.bloodbank.labservice.enums.InstrumentTypeEnum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "lab_instruments")
@FilterDef(name = "branchFilter", parameters = @ParamDef(name = "branchId", type = UUID.class))
@Filter(name = "branchFilter", condition = "branch_id = :branchId")
public class LabInstrument extends BranchScopedEntity {

    @Column(name = "instrument_code", nullable = false, unique = true, length = 50)
    private String instrumentCode;

    @Column(name = "instrument_name", nullable = false, length = 200)
    private String instrumentName;

    @Enumerated(EnumType.STRING)
    @Column(name = "instrument_type", nullable = false, length = 50)
    private InstrumentTypeEnum instrumentType;

    @Column(name = "manufacturer", length = 200)
    private String manufacturer;

    @Column(name = "model", length = 100)
    private String model;

    @Column(name = "serial_number", length = 100)
    private String serialNumber;

    @Column(name = "installation_date")
    private LocalDate installationDate;

    @Column(name = "last_calibration_date")
    private LocalDate lastCalibrationDate;

    @Column(name = "next_calibration_date")
    private LocalDate nextCalibrationDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private InstrumentStatusEnum status = InstrumentStatusEnum.ACTIVE;

    protected LabInstrument() {}

    public LabInstrument(String instrumentCode, String instrumentName,
                         InstrumentTypeEnum instrumentType) {
        this.instrumentCode = instrumentCode;
        this.instrumentName = instrumentName;
        this.instrumentType = instrumentType;
        this.status = InstrumentStatusEnum.ACTIVE;
    }

    public String getInstrumentCode() { return instrumentCode; }
    public void setInstrumentCode(String instrumentCode) { this.instrumentCode = instrumentCode; }

    public String getInstrumentName() { return instrumentName; }
    public void setInstrumentName(String instrumentName) { this.instrumentName = instrumentName; }

    public InstrumentTypeEnum getInstrumentType() { return instrumentType; }
    public void setInstrumentType(InstrumentTypeEnum instrumentType) { this.instrumentType = instrumentType; }

    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }

    public LocalDate getInstallationDate() { return installationDate; }
    public void setInstallationDate(LocalDate installationDate) { this.installationDate = installationDate; }

    public LocalDate getLastCalibrationDate() { return lastCalibrationDate; }
    public void setLastCalibrationDate(LocalDate lastCalibrationDate) { this.lastCalibrationDate = lastCalibrationDate; }

    public LocalDate getNextCalibrationDate() { return nextCalibrationDate; }
    public void setNextCalibrationDate(LocalDate nextCalibrationDate) { this.nextCalibrationDate = nextCalibrationDate; }

    public InstrumentStatusEnum getStatus() { return status; }
    public void setStatus(InstrumentStatusEnum status) { this.status = status; }
}
