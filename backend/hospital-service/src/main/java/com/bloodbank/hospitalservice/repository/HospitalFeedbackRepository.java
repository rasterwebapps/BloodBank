package com.bloodbank.hospitalservice.repository;

import com.bloodbank.hospitalservice.entity.HospitalFeedback;
import com.bloodbank.hospitalservice.enums.FeedbackCategoryEnum;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.QueryHint;

@Repository
public interface HospitalFeedbackRepository extends JpaRepository<HospitalFeedback, UUID> {

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    Page<HospitalFeedback> findByHospitalId(UUID hospitalId, Pageable pageable);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<HospitalFeedback> findByRequestId(UUID requestId);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    Page<HospitalFeedback> findByCategory(FeedbackCategoryEnum category, Pageable pageable);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<HospitalFeedback> findByHospitalIdAndResponseIsNull(UUID hospitalId);
}
