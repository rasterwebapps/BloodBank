package com.bloodbank.labservice.repository;

import com.bloodbank.common.model.enums.TestResultEnum;
import com.bloodbank.labservice.entity.TestResult;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.QueryHint;

@Repository
public interface TestResultRepository extends JpaRepository<TestResult, UUID> {

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<TestResult> findByTestOrderId(UUID testOrderId);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<TestResult> findByTestOrderIdAndResultStatus(UUID testOrderId, TestResultEnum resultStatus);

    boolean existsByTestOrderIdAndTestName(UUID testOrderId, String testName);
}
