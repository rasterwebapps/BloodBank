package com.bloodbank.donorservice.repository;

import com.bloodbank.donorservice.entity.DonorHealthRecord;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DonorHealthRecordRepository extends JpaRepository<DonorHealthRecord, UUID> {

    List<DonorHealthRecord> findByDonorIdOrderByScreeningDateDesc(UUID donorId);

    Optional<DonorHealthRecord> findFirstByDonorIdOrderByScreeningDateDesc(UUID donorId);
}
