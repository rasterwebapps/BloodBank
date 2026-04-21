package com.bloodbank.complianceservice.repository;

import com.bloodbank.complianceservice.entity.SopDocument;
import com.bloodbank.complianceservice.enums.SopCategoryEnum;
import com.bloodbank.complianceservice.enums.SopStatusEnum;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.QueryHint;

@Repository
public interface SopDocumentRepository extends JpaRepository<SopDocument, UUID>,
                                                JpaSpecificationExecutor<SopDocument> {

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<SopDocument> findByFrameworkId(UUID frameworkId);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<SopDocument> findByStatus(SopStatusEnum status);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<SopDocument> findByCategory(SopCategoryEnum category);
}
