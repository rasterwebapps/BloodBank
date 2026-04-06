package com.bloodbank.donorservice.mapper;

import com.bloodbank.donorservice.dto.AdverseReactionCreateRequest;
import com.bloodbank.donorservice.dto.AdverseReactionResponse;
import com.bloodbank.donorservice.dto.CollectionCreateRequest;
import com.bloodbank.donorservice.dto.CollectionResponse;
import com.bloodbank.donorservice.dto.CollectionSampleCreateRequest;
import com.bloodbank.donorservice.dto.CollectionSampleResponse;
import com.bloodbank.donorservice.entity.Collection;
import com.bloodbank.donorservice.entity.CollectionAdverseReaction;
import com.bloodbank.donorservice.entity.CollectionSample;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = DateTimeMapper.class)
public interface CollectionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "branchId", ignore = true)
    @Mapping(target = "collectionNumber", ignore = true)
    @Mapping(target = "collectionDate", ignore = true)
    @Mapping(target = "startTime", ignore = true)
    @Mapping(target = "endTime", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "volumeMl", ignore = true)
    @Mapping(target = "campCollectionId", ignore = true)
    Collection toEntity(CollectionCreateRequest request);

    CollectionResponse toResponse(Collection collection);

    List<CollectionResponse> toResponseList(List<Collection> collections);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "branchId", ignore = true)
    CollectionAdverseReaction toEntity(AdverseReactionCreateRequest request);

    AdverseReactionResponse toResponse(CollectionAdverseReaction reaction);

    List<AdverseReactionResponse> toReactionResponseList(List<CollectionAdverseReaction> reactions);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "branchId", ignore = true)
    @Mapping(target = "sampleNumber", ignore = true)
    @Mapping(target = "collectedAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    CollectionSample toEntity(CollectionSampleCreateRequest request);

    CollectionSampleResponse toResponse(CollectionSample sample);

    List<CollectionSampleResponse> toSampleResponseList(List<CollectionSample> samples);
}
