package com.bloodbank.labservice.repository;

import com.bloodbank.labservice.entity.LabInstrument;
import com.bloodbank.labservice.enums.InstrumentStatusEnum;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.QueryHint;

@Repository
public interface LabInstrumentRepository extends JpaRepository<LabInstrument, UUID> {

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    Optional<LabInstrument> findByInstrumentCode(String instrumentCode);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<LabInstrument> findByBranchId(UUID branchId);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<LabInstrument> findByStatus(InstrumentStatusEnum status);
}
