package com.bloodbank.labservice.mapper;

import com.bloodbank.labservice.dto.QualityControlCreateRequest;
import com.bloodbank.labservice.dto.QualityControlResponse;
import com.bloodbank.labservice.entity.QualityControlRecord;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface QualityControlMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "branchId", ignore = true)
    @Mapping(target = "qcDate", ignore = true)
    @Mapping(target = "status", ignore = true)
    QualityControlRecord toEntity(QualityControlCreateRequest request);

    QualityControlResponse toResponse(QualityControlRecord record);

    List<QualityControlResponse> toResponseList(List<QualityControlRecord> records);
}
