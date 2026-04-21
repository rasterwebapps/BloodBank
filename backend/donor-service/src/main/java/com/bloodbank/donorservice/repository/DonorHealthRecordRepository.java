package com.bloodbank.donorservice.repository;

import com.bloodbank.donorservice.entity.DonorHealthRecord;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.QueryHint;

@Repository
public interface DonorHealthRecordRepository extends JpaRepository<DonorHealthRecord, UUID> {

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<DonorHealthRecord> findByDonorIdOrderByScreeningDateDesc(UUID donorId);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    Optional<DonorHealthRecord> findFirstByDonorIdOrderByScreeningDateDesc(UUID donorId);
}
