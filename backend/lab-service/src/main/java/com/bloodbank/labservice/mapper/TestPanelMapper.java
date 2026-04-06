package com.bloodbank.labservice.mapper;

import com.bloodbank.labservice.dto.TestPanelCreateRequest;
import com.bloodbank.labservice.dto.TestPanelResponse;
import com.bloodbank.labservice.entity.TestPanel;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TestPanelMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "active", ignore = true)
    TestPanel toEntity(TestPanelCreateRequest request);

    TestPanelResponse toResponse(TestPanel testPanel);

    List<TestPanelResponse> toResponseList(List<TestPanel> testPanels);
}
