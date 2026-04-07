package com.bloodbank.documentservice.mapper;

import com.bloodbank.documentservice.dto.DocumentResponse;
import com.bloodbank.documentservice.dto.DocumentUploadRequest;
import com.bloodbank.documentservice.entity.Document;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DocumentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "documentCode", ignore = true)
    @Mapping(target = "mimeType", ignore = true)
    @Mapping(target = "fileSizeBytes", ignore = true)
    @Mapping(target = "storagePath", ignore = true)
    @Mapping(target = "storageBucket", ignore = true)
    @Mapping(target = "uploadedBy", ignore = true)
    @Mapping(target = "currentVersion", ignore = true)
    @Mapping(target = "status", ignore = true)
    Document toEntity(DocumentUploadRequest request);

    DocumentResponse toResponse(Document document);

    List<DocumentResponse> toResponseList(List<Document> documents);
}
