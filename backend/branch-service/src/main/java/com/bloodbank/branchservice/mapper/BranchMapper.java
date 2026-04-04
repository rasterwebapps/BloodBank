package com.bloodbank.branchservice.mapper;

import com.bloodbank.branchservice.dto.BranchCreateRequest;
import com.bloodbank.branchservice.dto.BranchEquipmentRequest;
import com.bloodbank.branchservice.dto.BranchEquipmentResponse;
import com.bloodbank.branchservice.dto.BranchOperatingHoursRequest;
import com.bloodbank.branchservice.dto.BranchOperatingHoursResponse;
import com.bloodbank.branchservice.dto.BranchRegionResponse;
import com.bloodbank.branchservice.dto.BranchResponse;
import com.bloodbank.branchservice.entity.Branch;
import com.bloodbank.branchservice.entity.BranchEquipment;
import com.bloodbank.branchservice.entity.BranchOperatingHours;
import com.bloodbank.branchservice.entity.BranchRegion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BranchMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "city", ignore = true)
    @Mapping(target = "parentBranch", ignore = true)
    @Mapping(target = "operatingHours", ignore = true)
    @Mapping(target = "equipment", ignore = true)
    @Mapping(target = "branchRegions", ignore = true)
    @Mapping(target = "status", constant = "ACTIVE")
    Branch toEntity(BranchCreateRequest request);

    @Mapping(source = "city.id", target = "cityId")
    @Mapping(source = "city.cityName", target = "cityName")
    @Mapping(source = "parentBranch.id", target = "parentBranchId")
    BranchResponse toResponse(Branch branch);

    List<BranchResponse> toResponseList(List<Branch> branches);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "branch", ignore = true)
    BranchOperatingHours toEntity(BranchOperatingHoursRequest request);

    @Mapping(source = "branch.id", target = "branchId")
    BranchOperatingHoursResponse toResponse(BranchOperatingHours hours);

    List<BranchOperatingHoursResponse> toOperatingHoursResponseList(List<BranchOperatingHours> hours);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "branch", ignore = true)
    BranchEquipment toEntity(BranchEquipmentRequest request);

    @Mapping(source = "branch.id", target = "branchId")
    BranchEquipmentResponse toResponse(BranchEquipment equipment);

    List<BranchEquipmentResponse> toEquipmentResponseList(List<BranchEquipment> equipment);

    @Mapping(source = "branch.id", target = "branchId")
    @Mapping(source = "region.id", target = "regionId")
    @Mapping(source = "region.regionName", target = "regionName")
    BranchRegionResponse toResponse(BranchRegion branchRegion);

    List<BranchRegionResponse> toBranchRegionResponseList(List<BranchRegion> branchRegions);
}
