package com.bloodbank.donorservice.mapper;

import com.bloodbank.donorservice.dto.DonorHealthRecordCreateRequest;
import com.bloodbank.donorservice.dto.DonorHealthRecordResponse;
import com.bloodbank.donorservice.entity.DonorHealthRecord;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = DateTimeMapper.class)
public interface DonorHealthRecordMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "branchId", ignore = true)
    @Mapping(target = "screeningDate", ignore = true)
    @Mapping(target = "eligible", ignore = true)
    DonorHealthRecord toEntity(DonorHealthRecordCreateRequest request);

    @Mapping(source = "eligible", target = "isEligible")
    DonorHealthRecordResponse toResponse(DonorHealthRecord record);

    List<DonorHealthRecordResponse> toResponseList(List<DonorHealthRecord> records);
}
