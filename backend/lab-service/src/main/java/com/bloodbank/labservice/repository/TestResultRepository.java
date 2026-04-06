package com.bloodbank.labservice.repository;

import com.bloodbank.common.model.enums.TestResultEnum;
import com.bloodbank.labservice.entity.TestResult;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TestResultRepository extends JpaRepository<TestResult, UUID> {

    List<TestResult> findByTestOrderId(UUID testOrderId);

    List<TestResult> findByTestOrderIdAndResultStatus(UUID testOrderId, TestResultEnum resultStatus);

    boolean existsByTestOrderIdAndTestName(UUID testOrderId, String testName);
}
