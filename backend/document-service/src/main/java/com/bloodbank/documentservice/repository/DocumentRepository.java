package com.bloodbank.documentservice.repository;

import com.bloodbank.documentservice.entity.Document;
import com.bloodbank.documentservice.enums.DocumentStatusEnum;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<Document, UUID> {

    Optional<Document> findByDocumentCode(String documentCode);

    List<Document> findByEntityTypeAndEntityIdAndStatus(String entityType, UUID entityId, DocumentStatusEnum status);

    List<Document> findByEntityIdAndStatus(UUID entityId, DocumentStatusEnum status);

    boolean existsByDocumentCode(String documentCode);
}
