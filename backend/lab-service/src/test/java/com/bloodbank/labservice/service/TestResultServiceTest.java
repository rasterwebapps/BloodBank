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
import com.bloodbank.labservice.enums.OrderPriorityEnum;
import com.bloodbank.labservice.enums.OrderStatusEnum;
import com.bloodbank.labservice.event.EventPublisher;
import com.bloodbank.labservice.mapper.TestResultMapper;
import com.bloodbank.labservice.repository.TestOrderRepository;
import com.bloodbank.labservice.repository.TestResultRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TestResultServiceTest {

    @Mock
    private TestResultRepository testResultRepository;

    @Mock
    private TestResultMapper testResultMapper;

    @Mock
    private TestOrderRepository testOrderRepository;

    @Mock
    private TestOrderService testOrderService;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private TestResultService testResultService;

    private UUID resultId;
    private UUID testOrderId;
    private UUID sampleId;
    private UUID branchId;
    private UUID donorId;
    private UUID instrumentId;
    private TestOrder testOrder;
    private TestResult testResult;
    private TestResultResponse testResultResponse;
    private TestResultCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        resultId = UUID.randomUUID();
        testOrderId = UUID.randomUUID();
        sampleId = UUID.randomUUID();
        branchId = UUID.randomUUID();
        donorId = UUID.randomUUID();
        instrumentId = UUID.randomUUID();

        testOrder = new TestOrder(sampleId, sampleId, donorId, OrderPriorityEnum.ROUTINE);
        testOrder.setId(testOrderId);
        testOrder.setBranchId(branchId);
        testOrder.setStatus(OrderStatusEnum.PENDING);
        testOrder.setOrderNumber("TO-ABCD1234");

        testResult = new TestResult(testOrderId, "HIV", TestResultEnum.NON_REACTIVE);
        testResult.setId(resultId);
        testResult.setTestedBy("tech1");
        testResult.setBranchId(branchId);
        testResult.setTestedAt(Instant.now());

        testResultResponse = new TestResultResponse(
                resultId, testOrderId, "HIV", "ELISA", "0.1",
                TestResultEnum.NON_REACTIVE, false, "S/CO", "0.0-0.9",
                instrumentId, "tech1", null, Instant.now(), null,
                "notes", branchId, LocalDateTime.now(), LocalDateTime.now()
        );

        createRequest = new TestResultCreateRequest(
                testOrderId, "HIV", "ELISA", "0.1",
                TestResultEnum.NON_REACTIVE, false, "S/CO", "0.0-0.9",
                instrumentId, "tech1", "notes", branchId
        );
    }

    @Nested
    @DisplayName("createResult")
    class CreateResult {

        @Test
        @DisplayName("should create result successfully")
        void shouldCreateResultSuccessfully() {
            when(testOrderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));
            when(testResultRepository.existsByTestOrderIdAndTestName(testOrderId, "HIV")).thenReturn(false);
            when(testResultMapper.toEntity(createRequest)).thenReturn(testResult);
            when(testResultRepository.save(any(TestResult.class))).thenReturn(testResult);
            when(testResultMapper.toResponse(testResult)).thenReturn(testResultResponse);

            TestResultResponse result = testResultService.createResult(createRequest);

            assertThat(result).isNotNull();
            assertThat(result.testName()).isEqualTo("HIV");
            verify(testResultRepository).save(any(TestResult.class));
            verify(eventPublisher).publishTestResultAvailable(any(TestResultAvailableEvent.class));
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when order not found")
        void shouldThrowResourceNotFoundWhenOrderNotFound() {
            when(testOrderRepository.findById(testOrderId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> testResultService.createResult(createRequest))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("should throw BusinessException when order is CANCELLED")
        void shouldThrowBusinessExceptionWhenOrderIsCancelled() {
            testOrder.setStatus(OrderStatusEnum.CANCELLED);
            when(testOrderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));

            assertThatThrownBy(() -> testResultService.createResult(createRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("cancelled");
        }

        @Test
        @DisplayName("should throw BusinessException when order is COMPLETED")
        void shouldThrowBusinessExceptionWhenOrderIsCompleted() {
            testOrder.setStatus(OrderStatusEnum.COMPLETED);
            when(testOrderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));

            assertThatThrownBy(() -> testResultService.createResult(createRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("completed");
        }

        @Test
        @DisplayName("should throw ConflictException when duplicate test name exists")
        void shouldThrowConflictExceptionWhenDuplicateTestName() {
            when(testOrderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));
            when(testResultRepository.existsByTestOrderIdAndTestName(testOrderId, "HIV")).thenReturn(true);

            assertThatThrownBy(() -> testResultService.createResult(createRequest))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("HIV");
        }

        @Test
        @DisplayName("should set isAbnormal to true when result is REACTIVE")
        void shouldSetIsAbnormalTrueWhenReactive() {
            TestResultCreateRequest reactiveRequest = new TestResultCreateRequest(
                    testOrderId, "HIV", "ELISA", "2.5",
                    TestResultEnum.REACTIVE, false, "S/CO", "0.0-0.9",
                    instrumentId, "tech1", "reactive result", branchId
            );

            TestResult reactiveResult = new TestResult(testOrderId, "HIV", TestResultEnum.REACTIVE);
            reactiveResult.setId(resultId);
            reactiveResult.setTestedBy("tech1");

            when(testOrderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));
            when(testResultRepository.existsByTestOrderIdAndTestName(testOrderId, "HIV")).thenReturn(false);
            when(testResultMapper.toEntity(reactiveRequest)).thenReturn(reactiveResult);
            when(testResultRepository.save(any(TestResult.class))).thenReturn(reactiveResult);
            when(testResultMapper.toResponse(reactiveResult)).thenReturn(testResultResponse);

            testResultService.createResult(reactiveRequest);

            assertThat(reactiveResult.isAbnormal()).isTrue();
        }

        @Test
        @DisplayName("should update order status to IN_PROGRESS when order is PENDING")
        void shouldUpdateOrderStatusToInProgressWhenPending() {
            when(testOrderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));
            when(testResultRepository.existsByTestOrderIdAndTestName(testOrderId, "HIV")).thenReturn(false);
            when(testResultMapper.toEntity(createRequest)).thenReturn(testResult);
            when(testResultRepository.save(any(TestResult.class))).thenReturn(testResult);
            when(testResultMapper.toResponse(testResult)).thenReturn(testResultResponse);

            testResultService.createResult(createRequest);

            verify(testOrderService).updateOrderStatus(testOrderId, OrderStatusEnum.IN_PROGRESS);
        }

        @Test
        @DisplayName("should not update order status when order is already IN_PROGRESS")
        void shouldNotUpdateOrderStatusWhenAlreadyInProgress() {
            testOrder.setStatus(OrderStatusEnum.IN_PROGRESS);

            when(testOrderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));
            when(testResultRepository.existsByTestOrderIdAndTestName(testOrderId, "HIV")).thenReturn(false);
            when(testResultMapper.toEntity(createRequest)).thenReturn(testResult);
            when(testResultRepository.save(any(TestResult.class))).thenReturn(testResult);
            when(testResultMapper.toResponse(testResult)).thenReturn(testResultResponse);

            testResultService.createResult(createRequest);

            verify(testOrderService, never()).updateOrderStatus(any(), any());
        }
    }

    @Nested
    @DisplayName("approveResult — Dual Review Logic")
    class ApproveResult {

        @Test
        @DisplayName("should approve result successfully with different verifier")
        void shouldApproveResultSuccessfully() {
            TestResultApprovalRequest approvalRequest = new TestResultApprovalRequest("reviewer1");
            testResult.setTestedBy("tech1");

            when(testResultRepository.findById(resultId)).thenReturn(Optional.of(testResult));
            when(testResultRepository.save(any(TestResult.class))).thenReturn(testResult);
            when(testResultMapper.toResponse(testResult)).thenReturn(testResultResponse);
            // For checkAllResultsVerifiedAndProcess: all verified, non-reactive
            when(testResultRepository.findByTestOrderId(testOrderId)).thenReturn(List.of(testResult));
            when(testOrderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));

            TestResultResponse result = testResultService.approveResult(resultId, approvalRequest);

            assertThat(result).isNotNull();
            assertThat(testResult.getVerifiedBy()).isEqualTo("reviewer1");
            assertThat(testResult.getVerifiedAt()).isNotNull();
        }

        @Test
        @DisplayName("should throw BusinessException for DUAL_REVIEW_VIOLATION when same person")
        void shouldThrowBusinessExceptionForDualReviewViolation() {
            TestResultApprovalRequest approvalRequest = new TestResultApprovalRequest("tech1");
            testResult.setTestedBy("tech1");

            when(testResultRepository.findById(resultId)).thenReturn(Optional.of(testResult));

            assertThatThrownBy(() -> testResultService.approveResult(resultId, approvalRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("dual review");
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when result not found")
        void shouldThrowResourceNotFoundWhenResultNotFound() {
            TestResultApprovalRequest approvalRequest = new TestResultApprovalRequest("reviewer1");
            when(testResultRepository.findById(resultId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> testResultService.approveResult(resultId, approvalRequest))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("should publish UnitReleasedEvent when all results verified and NON_REACTIVE")
        void shouldPublishUnitReleasedEventWhenAllVerifiedAndNonReactive() {
            TestResultApprovalRequest approvalRequest = new TestResultApprovalRequest("reviewer1");
            testResult.setTestedBy("tech1");
            testResult.setResultStatus(TestResultEnum.NON_REACTIVE);

            TestResult result2 = new TestResult(testOrderId, "HBV", TestResultEnum.NON_REACTIVE);
            result2.setId(UUID.randomUUID());
            result2.setTestedBy("tech2");
            result2.setVerifiedBy("reviewer2");

            // After approval, testResult gets verifiedBy set
            when(testResultRepository.findById(resultId)).thenReturn(Optional.of(testResult));
            when(testResultRepository.save(any(TestResult.class))).thenAnswer(inv -> {
                TestResult saved = inv.getArgument(0);
                saved.setVerifiedBy("reviewer1");
                return saved;
            });
            when(testResultMapper.toResponse(any(TestResult.class))).thenReturn(testResultResponse);

            // checkAllResultsVerifiedAndProcess
            when(testResultRepository.findByTestOrderId(testOrderId))
                    .thenReturn(List.of(testResult, result2));
            when(testOrderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));

            testResultService.approveResult(resultId, approvalRequest);

            verify(eventPublisher).publishUnitReleased(any(UnitReleasedEvent.class));
            verify(testOrderService).updateOrderStatus(testOrderId, OrderStatusEnum.COMPLETED);
        }

        @Test
        @DisplayName("should NOT publish UnitReleasedEvent when some results are REACTIVE")
        void shouldNotPublishUnitReleasedEventWhenSomeReactive() {
            TestResultApprovalRequest approvalRequest = new TestResultApprovalRequest("reviewer1");
            testResult.setTestedBy("tech1");
            testResult.setResultStatus(TestResultEnum.REACTIVE);

            TestResult result2 = new TestResult(testOrderId, "HBV", TestResultEnum.NON_REACTIVE);
            result2.setId(UUID.randomUUID());
            result2.setTestedBy("tech2");
            result2.setVerifiedBy("reviewer2");

            when(testResultRepository.findById(resultId)).thenReturn(Optional.of(testResult));
            when(testResultRepository.save(any(TestResult.class))).thenAnswer(inv -> {
                TestResult saved = inv.getArgument(0);
                saved.setVerifiedBy("reviewer1");
                return saved;
            });
            when(testResultMapper.toResponse(any(TestResult.class))).thenReturn(testResultResponse);

            // checkAllResultsVerifiedAndProcess
            when(testResultRepository.findByTestOrderId(testOrderId))
                    .thenReturn(List.of(testResult, result2));
            when(testOrderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));

            testResultService.approveResult(resultId, approvalRequest);

            verify(eventPublisher, never()).publishUnitReleased(any(UnitReleasedEvent.class));
            verify(testOrderService).updateOrderStatus(testOrderId, OrderStatusEnum.COMPLETED);
        }

        @Test
        @DisplayName("should NOT complete order when not all results verified")
        void shouldNotCompleteOrderWhenNotAllVerified() {
            TestResultApprovalRequest approvalRequest = new TestResultApprovalRequest("reviewer1");
            testResult.setTestedBy("tech1");
            testResult.setResultStatus(TestResultEnum.NON_REACTIVE);

            TestResult unverifiedResult = new TestResult(testOrderId, "HBV", TestResultEnum.NON_REACTIVE);
            unverifiedResult.setId(UUID.randomUUID());
            unverifiedResult.setTestedBy("tech2");
            // verifiedBy is null — not yet verified

            when(testResultRepository.findById(resultId)).thenReturn(Optional.of(testResult));
            when(testResultRepository.save(any(TestResult.class))).thenAnswer(inv -> {
                TestResult saved = inv.getArgument(0);
                saved.setVerifiedBy("reviewer1");
                return saved;
            });
            when(testResultMapper.toResponse(any(TestResult.class))).thenReturn(testResultResponse);

            // checkAllResultsVerifiedAndProcess — unverifiedResult has null verifiedBy
            when(testResultRepository.findByTestOrderId(testOrderId))
                    .thenReturn(List.of(testResult, unverifiedResult));

            testResultService.approveResult(resultId, approvalRequest);

            verify(eventPublisher, never()).publishUnitReleased(any(UnitReleasedEvent.class));
            verify(testOrderService, never()).updateOrderStatus(eq(testOrderId), eq(OrderStatusEnum.COMPLETED));
        }

        @Test
        @DisplayName("should allow approval when testedBy is null")
        void shouldAllowApprovalWhenTestedByIsNull() {
            TestResultApprovalRequest approvalRequest = new TestResultApprovalRequest("reviewer1");
            testResult.setTestedBy(null);

            when(testResultRepository.findById(resultId)).thenReturn(Optional.of(testResult));
            when(testResultRepository.save(any(TestResult.class))).thenAnswer(inv -> {
                TestResult saved = inv.getArgument(0);
                saved.setVerifiedBy("reviewer1");
                return saved;
            });
            when(testResultMapper.toResponse(any(TestResult.class))).thenReturn(testResultResponse);
            when(testResultRepository.findByTestOrderId(testOrderId)).thenReturn(List.of(testResult));
            when(testOrderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));

            TestResultResponse response = testResultService.approveResult(resultId, approvalRequest);

            assertThat(response).isNotNull();
            verify(testResultRepository).save(any(TestResult.class));
        }
    }

    @Nested
    @DisplayName("getResultsByOrderId")
    class GetResultsByOrderId {

        @Test
        @DisplayName("should return results by order id")
        void shouldReturnResultsByOrderId() {
            List<TestResult> results = List.of(testResult);
            List<TestResultResponse> responses = List.of(testResultResponse);
            when(testResultRepository.findByTestOrderId(testOrderId)).thenReturn(results);
            when(testResultMapper.toResponseList(results)).thenReturn(responses);

            List<TestResultResponse> result = testResultService.getResultsByOrderId(testOrderId);

            assertThat(result).hasSize(1);
            verify(testResultRepository).findByTestOrderId(testOrderId);
        }
    }

    @Nested
    @DisplayName("getResultById")
    class GetResultById {

        @Test
        @DisplayName("should return result when found")
        void shouldReturnResultWhenFound() {
            when(testResultRepository.findById(resultId)).thenReturn(Optional.of(testResult));
            when(testResultMapper.toResponse(testResult)).thenReturn(testResultResponse);

            TestResultResponse result = testResultService.getResultById(resultId);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(resultId);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when not found")
        void shouldThrowResourceNotFoundWhenNotFound() {
            when(testResultRepository.findById(resultId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> testResultService.getResultById(resultId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
