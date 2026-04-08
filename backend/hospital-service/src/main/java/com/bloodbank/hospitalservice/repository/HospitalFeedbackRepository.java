package com.bloodbank.hospitalservice.repository;

import com.bloodbank.hospitalservice.entity.HospitalFeedback;
import com.bloodbank.hospitalservice.enums.FeedbackCategoryEnum;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface HospitalFeedbackRepository extends JpaRepository<HospitalFeedback, UUID> {

    Page<HospitalFeedback> findByHospitalId(UUID hospitalId, Pageable pageable);

    List<HospitalFeedback> findByRequestId(UUID requestId);

    Page<HospitalFeedback> findByCategory(FeedbackCategoryEnum category, Pageable pageable);

    List<HospitalFeedback> findByHospitalIdAndResponseIsNull(UUID hospitalId);
}
