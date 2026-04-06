package com.bloodbank.labservice.repository;

import com.bloodbank.labservice.entity.QualityControlRecord;
import com.bloodbank.labservice.enums.QcStatusEnum;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QualityControlRecordRepository extends JpaRepository<QualityControlRecord, UUID> {

    List<QualityControlRecord> findByInstrumentId(UUID instrumentId);

    Page<QualityControlRecord> findByBranchId(UUID branchId, Pageable pageable);

    List<QualityControlRecord> findByStatus(QcStatusEnum status);
}
