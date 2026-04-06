package com.bloodbank.labservice.mapper;

import com.bloodbank.labservice.dto.TestResultCreateRequest;
import com.bloodbank.labservice.dto.TestResultResponse;
import com.bloodbank.labservice.entity.TestResult;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TestResultMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "branchId", ignore = true)
    @Mapping(target = "testedAt", ignore = true)
    @Mapping(target = "verifiedBy", ignore = true)
    @Mapping(target = "verifiedAt", ignore = true)
    TestResult toEntity(TestResultCreateRequest request);

    TestResultResponse toResponse(TestResult testResult);

    List<TestResultResponse> toResponseList(List<TestResult> testResults);
}
