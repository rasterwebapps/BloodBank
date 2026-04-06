package com.bloodbank.branchservice.entity;

import com.bloodbank.common.model.BaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "regions", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"country_id", "region_code"})
})
public class Region extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id", nullable = false)
    private Country country;

    @Column(name = "region_code", nullable = false, length = 10)
    private String regionCode;

    @Column(name = "region_name", nullable = false, length = 100)
    private String regionName;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "region", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<City> cities = new ArrayList<>();

    protected Region() {}

    public Region(Country country, String regionCode, String regionName) {
        this.country = country;
        this.regionCode = regionCode;
        this.regionName = regionName;
    }

    public Country getCountry() { return country; }
    public void setCountry(Country country) { this.country = country; }

    public String getRegionCode() { return regionCode; }
    public void setRegionCode(String regionCode) { this.regionCode = regionCode; }

    public String getRegionName() { return regionName; }
    public void setRegionName(String regionName) { this.regionName = regionName; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public List<City> getCities() { return cities; }
    public void setCities(List<City> cities) { this.cities = cities; }
}
