package com.bloodbank.labservice.service;

import com.bloodbank.common.events.TestResultAvailableEvent;
import com.bloodbank.common.events.UnitReleasedEvent;
import com.bloodbank.common.exceptions.BusinessException;
import com.bloodbank.common.exceptions.ConflictException;
import com.bloodbank.common.exceptions.ResourceNotFoundException;
import com.bloodbank.common.model.enums.TestResultEnum;
import com.bloodbank.labservice.dto.TestResultApprovalRequest;
import com.bloodbank.labservice.dto.TestResultCreateRequest;
import com.bloodbank.labservice.dto.TestResultResponse;
import com.bloodbank.labservice.entity.TestOrder;
import com.bloodbank.labservice.entity.TestResult;
import com.bloodbank.labservice.enums.OrderStatusEnum;
import com.bloodbank.labservice.event.EventPublisher;
import com.bloodbank.labservice.mapper.TestResultMapper;
import com.bloodbank.labservice.repository.TestOrderRepository;
import com.bloodbank.labservice.repository.TestResultRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class TestResultService {

    private static final Logger log = LoggerFactory.getLogger(TestResultService.class);

    private final TestResultRepository testResultRepository;
    private final TestResultMapper testResultMapper;
    private final TestOrderRepository testOrderRepository;
    private final TestOrderService testOrderService;
    private final EventPublisher eventPublisher;

    public TestResultService(TestResultRepository testResultRepository,
                             TestResultMapper testResultMapper,
                             TestOrderRepository testOrderRepository,
                             TestOrderService testOrderService,
                             EventPublisher eventPublisher) {
        this.testResultRepository = testResultRepository;
        this.testResultMapper = testResultMapper;
        this.testOrderRepository = testOrderRepository;
        this.testOrderService = testOrderService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public TestResultResponse createResult(TestResultCreateRequest request) {
        log.info("Creating test result for testOrderId={}, testName={}",
                request.testOrderId(), request.testName());

        TestOrder order = testOrderRepository.findById(request.testOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("TestOrder", "id", request.testOrderId()));

        if (order.getStatus() == OrderStatusEnum.CANCELLED) {
            throw new BusinessException("Cannot add result to a cancelled test order", "ORDER_CANCELLED");
        }

        if (testResultRepository.existsByTestOrderIdAndTestName(request.testOrderId(), request.testName())) {
            throw new ConflictException(
                    "Test result for test '" + request.testName() + "' already exists for order " + request.testOrderId());
        }

        TestResult result = testResultMapper.toEntity(request);
        result.setTestedAt(Instant.now());
        result.setBranchId(request.branchId());

        if (request.resultStatus() == TestResultEnum.REACTIVE) {
            result.setAbnormal(true);
        }

        result = testResultRepository.save(result);

        if (order.getStatus() == OrderStatusEnum.PENDING) {
            testOrderService.updateOrderStatus(order.getId(), OrderStatusEnum.IN_PROGRESS);
        }

        eventPublisher.publishTestResultAvailable(new TestResultAvailableEvent(
                order.getId(),
                order.getSampleId(),
                order.getBranchId(),
                Instant.now()
        ));

        return testResultMapper.toResponse(result);
    }

    @Transactional
    public TestResultResponse approveResult(UUID resultId, TestResultApprovalRequest request) {
        log.info("Approving test result id={}, verifiedBy={}", resultId, request.verifiedBy());

        TestResult result = testResultRepository.findById(resultId)
                .orElseThrow(() -> new ResourceNotFoundException("TestResult", "id", resultId));

        if (result.getTestedBy() != null && result.getTestedBy().equals(request.verifiedBy())) {
            throw new BusinessException(
                    "Verifier must be different from the person who performed the test (dual review required)",
                    "DUAL_REVIEW_VIOLATION"
            );
        }

        result.setVerifiedBy(request.verifiedBy());
        result.setVerifiedAt(Instant.now());
        result = testResultRepository.save(result);

        checkAllResultsVerifiedAndProcess(result.getTestOrderId());

        return testResultMapper.toResponse(result);
    }

    public List<TestResultResponse> getResultsByOrderId(UUID testOrderId) {
        return testResultMapper.toResponseList(testResultRepository.findByTestOrderId(testOrderId));
    }

    public TestResultResponse getResultById(UUID id) {
        TestResult result = testResultRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TestResult", "id", id));
        return testResultMapper.toResponse(result);
    }

    private void checkAllResultsVerifiedAndProcess(UUID testOrderId) {
        List<TestResult> results = testResultRepository.findByTestOrderId(testOrderId);

        boolean allVerified = results.stream()
                .allMatch(r -> r.getVerifiedBy() != null);

        if (!allVerified) {
            return;
        }

        boolean anyReactive = results.stream()
                .anyMatch(r -> r.getResultStatus() == TestResultEnum.REACTIVE);

        TestOrder order = testOrderRepository.findById(testOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("TestOrder", "id", testOrderId));

        if (anyReactive) {
            log.warn("Test order {} has REACTIVE results — blood unit {} quarantined",
                    testOrderId, order.getSampleId());
        } else {
            log.info("All results NON_REACTIVE for order {} — releasing blood unit {}",
                    testOrderId, order.getSampleId());
            eventPublisher.publishUnitReleased(new UnitReleasedEvent(
                    order.getSampleId(),
                    order.getBranchId(),
                    Instant.now()
            ));
        }

        testOrderService.updateOrderStatus(testOrderId, OrderStatusEnum.COMPLETED);
    }
}
