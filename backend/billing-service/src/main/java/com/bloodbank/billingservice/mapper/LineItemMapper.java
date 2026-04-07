package com.bloodbank.billingservice.mapper;

import com.bloodbank.billingservice.dto.LineItemCreateRequest;
import com.bloodbank.billingservice.dto.LineItemResponse;
import com.bloodbank.billingservice.entity.InvoiceLineItem;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LineItemMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "branchId", ignore = true)
    @Mapping(target = "taxAmount", ignore = true)
    @Mapping(target = "lineTotal", ignore = true)
    InvoiceLineItem toEntity(LineItemCreateRequest request);

    LineItemResponse toResponse(InvoiceLineItem lineItem);

    List<LineItemResponse> toResponseList(List<InvoiceLineItem> lineItems);
}
