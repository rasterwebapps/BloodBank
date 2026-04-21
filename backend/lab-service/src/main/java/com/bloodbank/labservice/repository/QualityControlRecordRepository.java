package com.bloodbank.labservice.repository;

import com.bloodbank.labservice.entity.QualityControlRecord;
import com.bloodbank.labservice.enums.QcStatusEnum;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.QueryHint;

@Repository
public interface QualityControlRecordRepository extends JpaRepository<QualityControlRecord, UUID> {

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<QualityControlRecord> findByInstrumentId(UUID instrumentId);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    Page<QualityControlRecord> findByBranchId(UUID branchId, Pageable pageable);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<QualityControlRecord> findByStatus(QcStatusEnum status);
}
