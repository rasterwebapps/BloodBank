package com.bloodbank.documentservice.mapper;

import com.bloodbank.documentservice.dto.DocumentVersionResponse;
import com.bloodbank.documentservice.entity.DocumentVersion;

import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DocumentVersionMapper {

    DocumentVersionResponse toResponse(DocumentVersion version);

    List<DocumentVersionResponse> toResponseList(List<DocumentVersion> versions);
}
