package com.bloodbank.billingservice.mapper;

import com.bloodbank.billingservice.dto.CreditNoteCreateRequest;
import com.bloodbank.billingservice.dto.CreditNoteResponse;
import com.bloodbank.billingservice.entity.CreditNote;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CreditNoteMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "branchId", ignore = true)
    @Mapping(target = "creditNoteNumber", ignore = true)
    @Mapping(target = "creditDate", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "appliedToInvoice", ignore = true)
    CreditNote toEntity(CreditNoteCreateRequest request);

    CreditNoteResponse toResponse(CreditNote creditNote);

    List<CreditNoteResponse> toResponseList(List<CreditNote> creditNotes);
}
