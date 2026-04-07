package com.bloodbank.billingservice.mapper;

import com.bloodbank.billingservice.dto.PaymentCreateRequest;
import com.bloodbank.billingservice.dto.PaymentResponse;
import com.bloodbank.billingservice.entity.Payment;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "branchId", ignore = true)
    @Mapping(target = "paymentNumber", ignore = true)
    @Mapping(target = "paymentDate", ignore = true)
    @Mapping(target = "status", ignore = true)
    Payment toEntity(PaymentCreateRequest request);

    PaymentResponse toResponse(Payment payment);

    List<PaymentResponse> toResponseList(List<Payment> payments);
}
