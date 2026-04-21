package com.bloodbank.reportingservice.repository;

import com.bloodbank.reportingservice.entity.DigitalSignature;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.QueryHint;

public interface DigitalSignatureRepository extends JpaRepository<DigitalSignature, UUID>,
                                                    JpaSpecificationExecutor<DigitalSignature> {

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<DigitalSignature> findByEntityTypeAndEntityIdOrderBySignedAtDesc(String entityType, UUID entityId);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<DigitalSignature> findBySignerIdOrderBySignedAtDesc(String signerId);
}
