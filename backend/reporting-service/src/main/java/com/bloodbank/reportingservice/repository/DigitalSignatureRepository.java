package com.bloodbank.reportingservice.repository;

import com.bloodbank.reportingservice.entity.DigitalSignature;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface DigitalSignatureRepository extends JpaRepository<DigitalSignature, UUID>,
                                                    JpaSpecificationExecutor<DigitalSignature> {

    List<DigitalSignature> findByEntityTypeAndEntityIdOrderBySignedAtDesc(String entityType, UUID entityId);

    List<DigitalSignature> findBySignerIdOrderBySignedAtDesc(String signerId);
}
