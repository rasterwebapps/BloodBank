package com.bloodbank.complianceservice.repository;

import com.bloodbank.complianceservice.entity.RecallRecord;
import com.bloodbank.complianceservice.enums.RecallStatusEnum;
import com.bloodbank.complianceservice.enums.RecallTypeEnum;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.QueryHint;

@Repository
public interface RecallRecordRepository extends JpaRepository<RecallRecord, UUID>,
                                                 JpaSpecificationExecutor<RecallRecord> {

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    Optional<RecallRecord> findByRecallNumber(String recallNumber);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<RecallRecord> findByStatus(RecallStatusEnum status);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<RecallRecord> findByRecallType(RecallTypeEnum recallType);
}
