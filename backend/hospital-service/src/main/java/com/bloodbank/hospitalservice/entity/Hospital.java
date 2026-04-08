package com.bloodbank.hospitalservice.entity;

import com.bloodbank.common.model.BranchScopedEntity;
import com.bloodbank.hospitalservice.enums.HospitalStatusEnum;
import com.bloodbank.hospitalservice.enums.HospitalTypeEnum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.util.UUID;

@Entity
@Table(name = "hospitals")
@FilterDef(name = "branchFilter", parameters = @ParamDef(name = "branchId", type = UUID.class))
@Filter(name = "branchFilter", condition = "branch_id = :branchId")
public class Hospital extends BranchScopedEntity {

    @Column(name = "hospital_code", nullable = false, unique = true, length = 30)
    private String hospitalCode;

    @Column(name = "hospital_name", nullable = false, length = 200)
    private String hospitalName;

    @Enumerated(EnumType.STRING)
    @Column(name = "hospital_type", nullable = false, length = 30)
    private HospitalTypeEnum hospitalType;

    @Column(name = "address_line1", length = 255)
    private String addressLine1;

    @Column(name = "address_line2", length = 255)
    private String addressLine2;

    @Column(name = "city_id")
    private UUID cityId;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "contact_person", length = 200)
    private String contactPerson;

    @Column(name = "license_number", length = 100)
    private String licenseNumber;

    @Column(name = "bed_count")
    private Integer bedCount;

    @Column(name = "has_blood_bank", nullable = false)
    private boolean hasBloodBank;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private HospitalStatusEnum status;

    public Hospital() {}

    public String getHospitalCode() { return hospitalCode; }
    public void setHospitalCode(String hospitalCode) { this.hospitalCode = hospitalCode; }

    public String getHospitalName() { return hospitalName; }
    public void setHospitalName(String hospitalName) { this.hospitalName = hospitalName; }

    public HospitalTypeEnum getHospitalType() { return hospitalType; }
    public void setHospitalType(HospitalTypeEnum hospitalType) { this.hospitalType = hospitalType; }

    public String getAddressLine1() { return addressLine1; }
    public void setAddressLine1(String addressLine1) { this.addressLine1 = addressLine1; }

    public String getAddressLine2() { return addressLine2; }
    public void setAddressLine2(String addressLine2) { this.addressLine2 = addressLine2; }

    public UUID getCityId() { return cityId; }
    public void setCityId(UUID cityId) { this.cityId = cityId; }

    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }

    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }

    public Integer getBedCount() { return bedCount; }
    public void setBedCount(Integer bedCount) { this.bedCount = bedCount; }

    public boolean isHasBloodBank() { return hasBloodBank; }
    public void setHasBloodBank(boolean hasBloodBank) { this.hasBloodBank = hasBloodBank; }

    public HospitalStatusEnum getStatus() { return status; }
    public void setStatus(HospitalStatusEnum status) { this.status = status; }
}
