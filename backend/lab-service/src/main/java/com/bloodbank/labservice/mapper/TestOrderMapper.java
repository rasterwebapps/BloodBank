package com.bloodbank.labservice.mapper;

import com.bloodbank.labservice.dto.TestOrderCreateRequest;
import com.bloodbank.labservice.dto.TestOrderResponse;
import com.bloodbank.labservice.entity.TestOrder;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TestOrderMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "branchId", ignore = true)
    @Mapping(target = "orderNumber", ignore = true)
    @Mapping(target = "orderDate", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    TestOrder toEntity(TestOrderCreateRequest request);

    TestOrderResponse toResponse(TestOrder testOrder);

    List<TestOrderResponse> toResponseList(List<TestOrder> testOrders);
}
