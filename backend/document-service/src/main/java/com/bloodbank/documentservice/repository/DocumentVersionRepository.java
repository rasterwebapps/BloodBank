package com.bloodbank.documentservice.repository;

import com.bloodbank.documentservice.entity.DocumentVersion;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.QueryHint;

public interface DocumentVersionRepository extends JpaRepository<DocumentVersion, UUID> {

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<DocumentVersion> findByDocumentIdOrderByVersionNumberDesc(UUID documentId);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    Optional<DocumentVersion> findByDocumentIdAndVersionNumber(UUID documentId, int versionNumber);
}
