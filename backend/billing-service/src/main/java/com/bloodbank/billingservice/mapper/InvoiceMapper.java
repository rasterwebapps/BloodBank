package com.bloodbank.billingservice.mapper;

import com.bloodbank.billingservice.dto.InvoiceCreateRequest;
import com.bloodbank.billingservice.dto.InvoiceResponse;
import com.bloodbank.billingservice.entity.Invoice;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface InvoiceMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "branchId", ignore = true)
    @Mapping(target = "invoiceNumber", ignore = true)
    @Mapping(target = "invoiceDate", ignore = true)
    @Mapping(target = "subtotal", ignore = true)
    @Mapping(target = "taxAmount", ignore = true)
    @Mapping(target = "discountAmount", ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    @Mapping(target = "amountPaid", ignore = true)
    @Mapping(target = "balanceDue", ignore = true)
    @Mapping(target = "status", ignore = true)
    Invoice toEntity(InvoiceCreateRequest request);

    InvoiceResponse toResponse(Invoice invoice);

    List<InvoiceResponse> toResponseList(List<Invoice> invoices);
}
