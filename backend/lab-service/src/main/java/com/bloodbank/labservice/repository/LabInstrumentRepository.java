package com.bloodbank.labservice.repository;

import com.bloodbank.labservice.entity.LabInstrument;
import com.bloodbank.labservice.enums.InstrumentStatusEnum;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LabInstrumentRepository extends JpaRepository<LabInstrument, UUID> {

    Optional<LabInstrument> findByInstrumentCode(String instrumentCode);

    List<LabInstrument> findByBranchId(UUID branchId);

    List<LabInstrument> findByStatus(InstrumentStatusEnum status);
}
