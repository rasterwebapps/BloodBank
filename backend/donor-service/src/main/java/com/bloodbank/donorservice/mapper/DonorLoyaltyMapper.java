package com.bloodbank.donorservice.mapper;

import com.bloodbank.donorservice.dto.DonorLoyaltyResponse;
import com.bloodbank.donorservice.entity.DonorLoyalty;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = DateTimeMapper.class)
public interface DonorLoyaltyMapper {

    DonorLoyaltyResponse toResponse(DonorLoyalty loyalty);

    List<DonorLoyaltyResponse> toResponseList(List<DonorLoyalty> loyalties);
}
