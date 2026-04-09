package com.bloodbank.complianceservice.mapper;

import com.bloodbank.complianceservice.dto.RecallCreateRequest;
import com.bloodbank.complianceservice.dto.RecallResponse;
import com.bloodbank.complianceservice.entity.RecallRecord;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RecallRecordMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "branchId", ignore = true)
    @Mapping(target = "recallNumber", ignore = true)
    @Mapping(target = "initiatedDate", ignore = true)
    @Mapping(target = "unitsRecovered", ignore = true)
    @Mapping(target = "unitsTransfused", ignore = true)
    @Mapping(target = "notificationSent", ignore = true)
    @Mapping(target = "closureDate", ignore = true)
    @Mapping(target = "closedBy", ignore = true)
    @Mapping(target = "status", ignore = true)
    RecallRecord toEntity(RecallCreateRequest request);

    RecallResponse toResponse(RecallRecord entity);

    List<RecallResponse> toResponseList(List<RecallRecord> entities);
}
