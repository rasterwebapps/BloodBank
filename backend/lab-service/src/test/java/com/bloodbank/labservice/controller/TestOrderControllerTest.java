package com.bloodbank.labservice.controller;

import com.bloodbank.common.dto.ApiResponse;
import com.bloodbank.common.exceptions.GlobalExceptionHandler;
import com.bloodbank.common.security.SecurityConfig;
import com.bloodbank.labservice.dto.TestOrderCreateRequest;
import com.bloodbank.labservice.dto.TestOrderResponse;
import com.bloodbank.labservice.enums.OrderPriorityEnum;
import com.bloodbank.labservice.enums.OrderStatusEnum;
import com.bloodbank.labservice.service.TestOrderService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = TestOrderController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = GlobalExceptionHandler.class))
@Import(SecurityConfig.class)
class TestOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TestOrderService testOrderService;

    @MockBean
    private JwtDecoder jwtDecoder;

    private static final String BASE_URL = "/api/v1/test-orders";
    private UUID orderId;
    private UUID sampleId;
    private UUID collectionId;
    private UUID donorId;
    private UUID panelId;
    private UUID branchId;
    private TestOrderResponse sampleResponse;
    private TestOrderCreateRequest sampleCreateRequest;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        sampleId = UUID.randomUUID();
        collectionId = UUID.randomUUID();
        donorId = UUID.randomUUID();
        panelId = UUID.randomUUID();
        branchId = UUID.randomUUID();

        sampleResponse = new TestOrderResponse(
                orderId, sampleId, collectionId, donorId, panelId,
                "TO-ABCD1234", Instant.now(), OrderPriorityEnum.ROUTINE,
                OrderStatusEnum.PENDING, "tech1", null, "notes",
                branchId, LocalDateTime.now(), LocalDateTime.now()
        );

        sampleCreateRequest = new TestOrderCreateRequest(
                sampleId, collectionId, donorId, panelId,
                OrderPriorityEnum.ROUTINE, "tech1", "notes", branchId
        );
    }

    @Nested
    @DisplayName("POST /api/v1/test-orders")
    class CreateOrder {

        @Test
        @DisplayName("should create order as LAB_TECHNICIAN — 201")
        @WithMockUser(roles = "LAB_TECHNICIAN")
        void shouldCreateOrderAsLabTechnician() throws Exception {
            when(testOrderService.createOrder(any(TestOrderCreateRequest.class))).thenReturn(sampleResponse);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.orderNumber").value("TO-ABCD1234"));
        }

        @Test
        @DisplayName("should create order as BRANCH_MANAGER — 201")
        @WithMockUser(roles = "BRANCH_MANAGER")
        void shouldCreateOrderAsBranchManager() throws Exception {
            when(testOrderService.createOrder(any(TestOrderCreateRequest.class))).thenReturn(sampleResponse);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("should reject as DONOR — 403")
        @WithMockUser(roles = "DONOR")
        void shouldRejectAsDonor() throws Exception {
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/test-orders/{id}")
    class GetOrderById {

        @Test
        @DisplayName("should return order as LAB_TECHNICIAN — 200")
        @WithMockUser(roles = "LAB_TECHNICIAN")
        void shouldReturnOrderAsLabTechnician() throws Exception {
            when(testOrderService.getOrderById(orderId)).thenReturn(sampleResponse);

            mockMvc.perform(get(BASE_URL + "/{id}", orderId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(orderId.toString()));
        }

        @Test
        @DisplayName("should reject as unauthorized role — 403")
        @WithMockUser(roles = "DONOR")
        void shouldRejectAsUnauthorizedRole() throws Exception {
            mockMvc.perform(get(BASE_URL + "/{id}", orderId))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/test-orders/status/{status}")
    class GetOrdersByStatus {

        @Test
        @DisplayName("should return orders by status as BRANCH_MANAGER — 200")
        @WithMockUser(roles = "BRANCH_MANAGER")
        void shouldReturnOrdersByStatusAsBranchManager() throws Exception {
            when(testOrderService.getOrdersByStatus(OrderStatusEnum.PENDING))
                    .thenReturn(List.of(sampleResponse));

            mockMvc.perform(get(BASE_URL + "/status/{status}", "PENDING"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/test-orders/{id}/status")
    class UpdateOrderStatus {

        @Test
        @DisplayName("should update order status as LAB_TECHNICIAN — 200")
        @WithMockUser(roles = "LAB_TECHNICIAN")
        void shouldUpdateOrderStatusAsLabTechnician() throws Exception {
            when(testOrderService.updateOrderStatus(eq(orderId), eq(OrderStatusEnum.IN_PROGRESS)))
                    .thenReturn(sampleResponse);

            mockMvc.perform(patch(BASE_URL + "/{id}/status", orderId)
                            .param("status", "IN_PROGRESS"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("should reject as unauthorized role — 403")
        @WithMockUser(roles = "DONOR")
        void shouldRejectUpdateAsUnauthorizedRole() throws Exception {
            mockMvc.perform(patch(BASE_URL + "/{id}/status", orderId)
                            .param("status", "IN_PROGRESS"))
                    .andExpect(status().isForbidden());
        }
    }
}
