package com.bloodbank.labservice.service;

import com.bloodbank.common.events.DonationCompletedEvent;
import com.bloodbank.common.exceptions.ResourceNotFoundException;
import com.bloodbank.labservice.dto.TestOrderCreateRequest;
import com.bloodbank.labservice.dto.TestOrderResponse;
import com.bloodbank.labservice.entity.TestOrder;
import com.bloodbank.labservice.entity.TestPanel;
import com.bloodbank.labservice.enums.OrderPriorityEnum;
import com.bloodbank.labservice.enums.OrderStatusEnum;
import com.bloodbank.labservice.mapper.TestOrderMapper;
import com.bloodbank.labservice.repository.TestOrderRepository;
import com.bloodbank.labservice.repository.TestPanelRepository;

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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TestOrderServiceTest {

    @Mock
    private TestOrderRepository testOrderRepository;

    @Mock
    private TestOrderMapper testOrderMapper;

    @Mock
    private TestPanelRepository testPanelRepository;

    @InjectMocks
    private TestOrderService testOrderService;

    private UUID orderId;
    private UUID sampleId;
    private UUID collectionId;
    private UUID donorId;
    private UUID panelId;
    private UUID branchId;
    private TestOrder testOrder;
    private TestOrderResponse testOrderResponse;
    private TestOrderCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        sampleId = UUID.randomUUID();
        collectionId = UUID.randomUUID();
        donorId = UUID.randomUUID();
        panelId = UUID.randomUUID();
        branchId = UUID.randomUUID();

        testOrder = new TestOrder(sampleId, collectionId, donorId, OrderPriorityEnum.ROUTINE);
        testOrder.setId(orderId);
        testOrder.setOrderNumber("TO-ABCD1234");
        testOrder.setBranchId(branchId);
        testOrder.setStatus(OrderStatusEnum.PENDING);
        testOrder.setOrderDate(Instant.now());

        testOrderResponse = new TestOrderResponse(
                orderId, sampleId, collectionId, donorId, panelId,
                "TO-ABCD1234", Instant.now(), OrderPriorityEnum.ROUTINE,
                OrderStatusEnum.PENDING, "tech1", null, "notes",
                branchId, LocalDateTime.now(), LocalDateTime.now()
        );

        createRequest = new TestOrderCreateRequest(
                sampleId, collectionId, donorId, panelId,
                OrderPriorityEnum.ROUTINE, "tech1", "notes", branchId
        );
    }

    @Nested
    @DisplayName("createOrder")
    class CreateOrder {

        @Test
        @DisplayName("should create order successfully")
        void shouldCreateOrderSuccessfully() {
            when(testOrderMapper.toEntity(createRequest)).thenReturn(testOrder);
            when(testOrderRepository.save(any(TestOrder.class))).thenReturn(testOrder);
            when(testOrderMapper.toResponse(testOrder)).thenReturn(testOrderResponse);

            TestOrderResponse result = testOrderService.createOrder(createRequest);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(orderId);
            verify(testOrderRepository).save(any(TestOrder.class));
            verify(testOrderMapper).toEntity(createRequest);
            verify(testOrderMapper).toResponse(testOrder);
        }

        @Test
        @DisplayName("should set PENDING status and generate order number")
        void shouldSetPendingStatusAndGenerateOrderNumber() {
            TestOrder capturedOrder = new TestOrder(sampleId, collectionId, donorId, OrderPriorityEnum.ROUTINE);
            when(testOrderMapper.toEntity(createRequest)).thenReturn(capturedOrder);
            when(testOrderRepository.save(any(TestOrder.class))).thenAnswer(inv -> inv.getArgument(0));
            when(testOrderMapper.toResponse(any(TestOrder.class))).thenReturn(testOrderResponse);

            testOrderService.createOrder(createRequest);

            assertThat(capturedOrder.getStatus()).isEqualTo(OrderStatusEnum.PENDING);
            assertThat(capturedOrder.getOrderNumber()).startsWith("TO-");
            assertThat(capturedOrder.getOrderDate()).isNotNull();
            assertThat(capturedOrder.getBranchId()).isEqualTo(branchId);
        }
    }

    @Nested
    @DisplayName("getOrderById")
    class GetOrderById {

        @Test
        @DisplayName("should return order when found")
        void shouldReturnOrderWhenFound() {
            when(testOrderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
            when(testOrderMapper.toResponse(testOrder)).thenReturn(testOrderResponse);

            TestOrderResponse result = testOrderService.getOrderById(orderId);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(orderId);
            verify(testOrderRepository).findById(orderId);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when not found")
        void shouldThrowResourceNotFoundExceptionWhenNotFound() {
            when(testOrderRepository.findById(orderId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> testOrderService.getOrderById(orderId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getOrdersByStatus")
    class GetOrdersByStatus {

        @Test
        @DisplayName("should return orders by status")
        void shouldReturnOrdersByStatus() {
            List<TestOrder> orders = List.of(testOrder);
            List<TestOrderResponse> responses = List.of(testOrderResponse);
            when(testOrderRepository.findByStatus(OrderStatusEnum.PENDING)).thenReturn(orders);
            when(testOrderMapper.toResponseList(orders)).thenReturn(responses);

            List<TestOrderResponse> result = testOrderService.getOrdersByStatus(OrderStatusEnum.PENDING);

            assertThat(result).hasSize(1);
            verify(testOrderRepository).findByStatus(OrderStatusEnum.PENDING);
        }
    }

    @Nested
    @DisplayName("getOrdersByDonorId")
    class GetOrdersByDonorId {

        @Test
        @DisplayName("should return orders by donor id")
        void shouldReturnOrdersByDonorId() {
            List<TestOrder> orders = List.of(testOrder);
            List<TestOrderResponse> responses = List.of(testOrderResponse);
            when(testOrderRepository.findByDonorId(donorId)).thenReturn(orders);
            when(testOrderMapper.toResponseList(orders)).thenReturn(responses);

            List<TestOrderResponse> result = testOrderService.getOrdersByDonorId(donorId);

            assertThat(result).hasSize(1);
            verify(testOrderRepository).findByDonorId(donorId);
        }
    }

    @Nested
    @DisplayName("updateOrderStatus")
    class UpdateOrderStatus {

        @Test
        @DisplayName("should update order status successfully")
        void shouldUpdateOrderStatusSuccessfully() {
            when(testOrderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
            when(testOrderRepository.save(any(TestOrder.class))).thenReturn(testOrder);
            when(testOrderMapper.toResponse(testOrder)).thenReturn(testOrderResponse);

            TestOrderResponse result = testOrderService.updateOrderStatus(orderId, OrderStatusEnum.IN_PROGRESS);

            assertThat(result).isNotNull();
            verify(testOrderRepository).save(testOrder);
        }

        @Test
        @DisplayName("should set completedAt when status is COMPLETED")
        void shouldSetCompletedAtWhenStatusIsCompleted() {
            when(testOrderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
            when(testOrderRepository.save(any(TestOrder.class))).thenReturn(testOrder);
            when(testOrderMapper.toResponse(testOrder)).thenReturn(testOrderResponse);

            testOrderService.updateOrderStatus(orderId, OrderStatusEnum.COMPLETED);

            assertThat(testOrder.getCompletedAt()).isNotNull();
            assertThat(testOrder.getStatus()).isEqualTo(OrderStatusEnum.COMPLETED);
        }

        @Test
        @DisplayName("should not set completedAt when status is not COMPLETED")
        void shouldNotSetCompletedAtWhenStatusIsNotCompleted() {
            when(testOrderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
            when(testOrderRepository.save(any(TestOrder.class))).thenReturn(testOrder);
            when(testOrderMapper.toResponse(testOrder)).thenReturn(testOrderResponse);

            testOrderService.updateOrderStatus(orderId, OrderStatusEnum.IN_PROGRESS);

            assertThat(testOrder.getCompletedAt()).isNull();
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when order not found")
        void shouldThrowResourceNotFoundExceptionWhenOrderNotFound() {
            when(testOrderRepository.findById(orderId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> testOrderService.updateOrderStatus(orderId, OrderStatusEnum.IN_PROGRESS))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("createOrderFromDonation")
    class CreateOrderFromDonation {

        @Test
        @DisplayName("should create order from donation with mandatory panel")
        void shouldCreateOrderFromDonationWithMandatoryPanel() {
            UUID donationId = UUID.randomUUID();
            DonationCompletedEvent event = new DonationCompletedEvent(
                    donationId, donorId, branchId, Instant.now()
            );

            TestPanel mandatoryPanel = new TestPanel("TTI", "TTI Panel", "Mandatory TTI tests",
                    "HIV,HBV,HCV,Syphilis", true);
            mandatoryPanel.setId(panelId);

            when(testPanelRepository.findByIsMandatoryTrue()).thenReturn(List.of(mandatoryPanel));
            when(testOrderRepository.save(any(TestOrder.class))).thenAnswer(inv -> inv.getArgument(0));

            testOrderService.createOrderFromDonation(event);

            ArgumentCaptor<TestOrder> captor = ArgumentCaptor.forClass(TestOrder.class);
            verify(testOrderRepository).save(captor.capture());

            TestOrder savedOrder = captor.getValue();
            assertThat(savedOrder.getSampleId()).isEqualTo(donationId);
            assertThat(savedOrder.getCollectionId()).isEqualTo(donationId);
            assertThat(savedOrder.getDonorId()).isEqualTo(donorId);
            assertThat(savedOrder.getBranchId()).isEqualTo(branchId);
            assertThat(savedOrder.getPanelId()).isEqualTo(panelId);
            assertThat(savedOrder.getOrderedBy()).isEqualTo("SYSTEM");
            assertThat(savedOrder.getOrderNumber()).startsWith("TO-");
            assertThat(savedOrder.getPriority()).isEqualTo(OrderPriorityEnum.ROUTINE);
        }

        @Test
        @DisplayName("should create order without panel when no mandatory panels configured")
        void shouldCreateOrderWithoutPanelWhenNoMandatoryPanelsConfigured() {
            UUID donationId = UUID.randomUUID();
            DonationCompletedEvent event = new DonationCompletedEvent(
                    donationId, donorId, branchId, Instant.now()
            );

            when(testPanelRepository.findByIsMandatoryTrue()).thenReturn(List.of());
            when(testOrderRepository.save(any(TestOrder.class))).thenAnswer(inv -> inv.getArgument(0));

            testOrderService.createOrderFromDonation(event);

            ArgumentCaptor<TestOrder> captor = ArgumentCaptor.forClass(TestOrder.class);
            verify(testOrderRepository).save(captor.capture());

            TestOrder savedOrder = captor.getValue();
            assertThat(savedOrder.getPanelId()).isNull();
        }
    }
}
