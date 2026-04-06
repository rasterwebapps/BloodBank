package com.bloodbank.labservice.service;

import com.bloodbank.common.dto.PagedResponse;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class TestOrderService {

    private static final Logger log = LoggerFactory.getLogger(TestOrderService.class);

    private final TestOrderRepository testOrderRepository;
    private final TestOrderMapper testOrderMapper;
    private final TestPanelRepository testPanelRepository;

    public TestOrderService(TestOrderRepository testOrderRepository,
                            TestOrderMapper testOrderMapper,
                            TestPanelRepository testPanelRepository) {
        this.testOrderRepository = testOrderRepository;
        this.testOrderMapper = testOrderMapper;
        this.testPanelRepository = testPanelRepository;
    }

    @Transactional
    public TestOrderResponse createOrder(TestOrderCreateRequest request) {
        log.info("Creating test order for sampleId={}, donorId={}", request.sampleId(), request.donorId());
        TestOrder order = testOrderMapper.toEntity(request);
        order.setOrderNumber("TO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        order.setStatus(OrderStatusEnum.PENDING);
        order.setOrderDate(Instant.now());
        order.setBranchId(request.branchId());
        order = testOrderRepository.save(order);
        return testOrderMapper.toResponse(order);
    }

    public TestOrderResponse getOrderById(UUID id) {
        TestOrder order = testOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TestOrder", "id", id));
        return testOrderMapper.toResponse(order);
    }

    public PagedResponse<TestOrderResponse> getOrdersByBranchId(UUID branchId, Pageable pageable) {
        Page<TestOrder> page = testOrderRepository.findByBranchId(branchId, pageable);
        List<TestOrderResponse> content = testOrderMapper.toResponseList(page.getContent());
        return new PagedResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    public List<TestOrderResponse> getOrdersByStatus(OrderStatusEnum status) {
        return testOrderMapper.toResponseList(testOrderRepository.findByStatus(status));
    }

    public List<TestOrderResponse> getOrdersByDonorId(UUID donorId) {
        return testOrderMapper.toResponseList(testOrderRepository.findByDonorId(donorId));
    }

    @Transactional
    public TestOrderResponse updateOrderStatus(UUID id, OrderStatusEnum status) {
        TestOrder order = testOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TestOrder", "id", id));
        order.setStatus(status);
        if (status == OrderStatusEnum.COMPLETED) {
            order.setCompletedAt(Instant.now());
        }
        order = testOrderRepository.save(order);
        log.info("Updated test order {} status to {}", id, status);
        return testOrderMapper.toResponse(order);
    }

    @Transactional
    public void createOrderFromDonation(DonationCompletedEvent event) {
        log.info("Creating test order from DonationCompletedEvent: donationId={}, donorId={}",
                event.donationId(), event.donorId());

        TestOrder order = new TestOrder(
                event.donationId(),
                event.donationId(),
                event.donorId(),
                OrderPriorityEnum.ROUTINE
        );
        order.setOrderNumber("TO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        order.setBranchId(event.branchId());
        order.setOrderedBy("SYSTEM");

        List<TestPanel> mandatoryPanels = testPanelRepository.findByIsMandatoryTrue();
        if (!mandatoryPanels.isEmpty()) {
            order.setPanelId(mandatoryPanels.getFirst().getId());
        }

        testOrderRepository.save(order);
        log.info("Test order created from donation: orderNumber={}", order.getOrderNumber());
    }
}
