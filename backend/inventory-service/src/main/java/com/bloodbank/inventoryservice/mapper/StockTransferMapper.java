package com.bloodbank.inventoryservice.mapper;

import com.bloodbank.inventoryservice.dto.StockTransferCreateRequest;
import com.bloodbank.inventoryservice.dto.StockTransferResponse;
import com.bloodbank.inventoryservice.entity.StockTransfer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StockTransferMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "transferNumber", ignore = true)
    @Mapping(target = "requestDate", ignore = true)
    @Mapping(target = "shippedDate", ignore = true)
    @Mapping(target = "receivedDate", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "approvedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    StockTransfer toEntity(StockTransferCreateRequest request);

    StockTransferResponse toResponse(StockTransfer entity);

    List<StockTransferResponse> toResponseList(List<StockTransfer> entities);
}
