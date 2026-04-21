package com.bloodbank.documentservice.repository;

import com.bloodbank.documentservice.entity.Document;
import com.bloodbank.documentservice.enums.DocumentStatusEnum;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.QueryHint;

public interface DocumentRepository extends JpaRepository<Document, UUID> {

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    Optional<Document> findByDocumentCode(String documentCode);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<Document> findByEntityTypeAndEntityIdAndStatus(String entityType, UUID entityId, DocumentStatusEnum status);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<Document> findByEntityIdAndStatus(UUID entityId, DocumentStatusEnum status);

    boolean existsByDocumentCode(String documentCode);
}
