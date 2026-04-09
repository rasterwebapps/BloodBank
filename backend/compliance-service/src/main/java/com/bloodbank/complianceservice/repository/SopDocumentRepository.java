package com.bloodbank.complianceservice.repository;

import com.bloodbank.complianceservice.entity.SopDocument;
import com.bloodbank.complianceservice.enums.SopCategoryEnum;
import com.bloodbank.complianceservice.enums.SopStatusEnum;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SopDocumentRepository extends JpaRepository<SopDocument, UUID>,
                                                JpaSpecificationExecutor<SopDocument> {

    List<SopDocument> findByFrameworkId(UUID frameworkId);

    List<SopDocument> findByStatus(SopStatusEnum status);

    List<SopDocument> findByCategory(SopCategoryEnum category);
}
