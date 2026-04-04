package com.bloodbank.branchservice.mapper;

import com.bloodbank.branchservice.dto.BloodGroupResponse;
import com.bloodbank.branchservice.dto.CityResponse;
import com.bloodbank.branchservice.dto.ComponentTypeResponse;
import com.bloodbank.branchservice.dto.CountryResponse;
import com.bloodbank.branchservice.dto.DeferralReasonResponse;
import com.bloodbank.branchservice.dto.IcdCodeResponse;
import com.bloodbank.branchservice.dto.ReactionTypeResponse;
import com.bloodbank.branchservice.dto.RegionResponse;
import com.bloodbank.branchservice.entity.BloodGroup;
import com.bloodbank.branchservice.entity.City;
import com.bloodbank.branchservice.entity.ComponentType;
import com.bloodbank.branchservice.entity.Country;
import com.bloodbank.branchservice.entity.DeferralReason;
import com.bloodbank.branchservice.entity.IcdCode;
import com.bloodbank.branchservice.entity.ReactionType;
import com.bloodbank.branchservice.entity.Region;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MasterDataMapper {

    BloodGroupResponse toResponse(BloodGroup bloodGroup);
    List<BloodGroupResponse> toBloodGroupResponseList(List<BloodGroup> bloodGroups);

    ComponentTypeResponse toResponse(ComponentType componentType);
    List<ComponentTypeResponse> toComponentTypeResponseList(List<ComponentType> componentTypes);

    CountryResponse toResponse(Country country);
    List<CountryResponse> toCountryResponseList(List<Country> countries);

    @Mapping(source = "country.id", target = "countryId")
    @Mapping(source = "country.countryName", target = "countryName")
    RegionResponse toResponse(Region region);
    List<RegionResponse> toRegionResponseList(List<Region> regions);

    @Mapping(source = "region.id", target = "regionId")
    @Mapping(source = "region.regionName", target = "regionName")
    CityResponse toResponse(City city);
    List<CityResponse> toCityResponseList(List<City> cities);

    DeferralReasonResponse toResponse(DeferralReason deferralReason);
    List<DeferralReasonResponse> toDeferralReasonResponseList(List<DeferralReason> deferralReasons);

    ReactionTypeResponse toResponse(ReactionType reactionType);
    List<ReactionTypeResponse> toReactionTypeResponseList(List<ReactionType> reactionTypes);

    IcdCodeResponse toResponse(IcdCode icdCode);
    List<IcdCodeResponse> toIcdCodeResponseList(List<IcdCode> icdCodes);
}
