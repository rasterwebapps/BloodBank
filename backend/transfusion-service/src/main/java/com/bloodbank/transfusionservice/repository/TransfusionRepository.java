package com.bloodbank.transfusionservice.repository;

import com.bloodbank.transfusionservice.entity.Transfusion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TransfusionRepository extends JpaRepository<Transfusion, UUID> {
    Page<Transfusion> findByPatientId(String patientId, Pageable pageable);
}
