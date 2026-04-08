package com.bloodbank.hospitalservice.controller;

import com.bloodbank.common.dto.ApiResponse;
import com.bloodbank.common.dto.PagedResponse;
import com.bloodbank.common.exceptions.GlobalExceptionHandler;
import com.bloodbank.common.security.SecurityConfig;
import com.bloodbank.hospitalservice.dto.HospitalContractCreateRequest;
import com.bloodbank.hospitalservice.dto.HospitalContractResponse;
import com.bloodbank.hospitalservice.enums.ContractStatusEnum;
import com.bloodbank.hospitalservice.service.ContractService;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = HospitalContractController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = GlobalExceptionHandler.class))
@Import(SecurityConfig.class)
class HospitalContractControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ContractService contractService;

    @MockBean
    private JwtDecoder jwtDecoder;

    private UUID hospitalId;
    private UUID contractId;
    private UUID branchId;
    private String baseUrl;
    private HospitalContractResponse sampleResponse;
    private HospitalContractCreateRequest sampleCreateRequest;

    @BeforeEach
    void setUp() {
        hospitalId = UUID.randomUUID();
        contractId = UUID.randomUUID();
        branchId = UUID.randomUUID();
        baseUrl = "/api/v1/hospitals/" + hospitalId + "/contracts";

        sampleResponse = new HospitalContractResponse(
                contractId, hospitalId, "CTR-001",
                LocalDate.of(2024, 1, 1), LocalDate.of(2025, 12, 31),
                new BigDecimal("10.00"), 30, new BigDecimal("100000.00"),
                true, ContractStatusEnum.ACTIVE, null, "Standard contract",
                branchId, LocalDateTime.now(), LocalDateTime.now()
        );

        sampleCreateRequest = new HospitalContractCreateRequest(
                hospitalId, "CTR-001",
                LocalDate.of(2024, 1, 1), LocalDate.of(2025, 12, 31),
                new BigDecimal("10.00"), 30, new BigDecimal("100000.00"),
                true, null, "Standard contract", branchId
        );
    }

    // ==================== CREATE CONTRACT ====================

    @Nested
    @DisplayName("POST /api/v1/hospitals/{hospitalId}/contracts")
    class CreateContract {

        @Test
        @WithMockUser(roles = {"BRANCH_ADMIN"})
        @DisplayName("Should create contract as BRANCH_ADMIN - 201")
        void createContract_asBranchAdmin_returns201() throws Exception {
            when(contractService.createContract(any(HospitalContractCreateRequest.class)))
                    .thenReturn(sampleResponse);

            mockMvc.perform(post(baseUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.contractNumber").value("CTR-001"))
                    .andExpect(jsonPath("$.message").value("Contract created successfully"));
        }

        @Test
        @WithMockUser(roles = {"BRANCH_MANAGER"})
        @DisplayName("Should create contract as BRANCH_MANAGER - 201")
        void createContract_asBranchManager_returns201() throws Exception {
            when(contractService.createContract(any(HospitalContractCreateRequest.class)))
                    .thenReturn(sampleResponse);

            mockMvc.perform(post(baseUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isCreated());
        }

        @Test
        @WithMockUser(roles = {"HOSPITAL_USER"})
        @DisplayName("Should deny HOSPITAL_USER from creating contract - 403")
        void createContract_asHospitalUser_returns403() throws Exception {
            mockMvc.perform(post(baseUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = {"DONOR"})
        @DisplayName("Should deny DONOR from creating contract - 403")
        void createContract_asDonor_returns403() throws Exception {
            mockMvc.perform(post(baseUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 401 for unauthenticated request")
        void createContract_unauthenticated_returns401() throws Exception {
            mockMvc.perform(post(baseUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ==================== GET CONTRACTS ====================

    @Nested
    @DisplayName("GET /api/v1/hospitals/{hospitalId}/contracts")
    class GetContracts {

        @Test
        @WithMockUser(roles = {"BRANCH_ADMIN"})
        @DisplayName("Should list contracts as BRANCH_ADMIN - 200")
        void getContracts_asBranchAdmin_returns200() throws Exception {
            PagedResponse<HospitalContractResponse> pagedResponse = new PagedResponse<>(
                    List.of(sampleResponse), 0, 20, 1, 1, true);
            when(contractService.getContractsByHospitalId(eq(hospitalId), any())).thenReturn(pagedResponse);

            mockMvc.perform(get(baseUrl))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.totalElements").value(1));
        }

        @Test
        @WithMockUser(roles = {"AUDITOR"})
        @DisplayName("Should list contracts as AUDITOR - 200")
        void getContracts_asAuditor_returns200() throws Exception {
            PagedResponse<HospitalContractResponse> pagedResponse = new PagedResponse<>(
                    List.of(sampleResponse), 0, 20, 1, 1, true);
            when(contractService.getContractsByHospitalId(eq(hospitalId), any())).thenReturn(pagedResponse);

            mockMvc.perform(get(baseUrl))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = {"DONOR"})
        @DisplayName("Should deny DONOR from listing contracts - 403")
        void getContracts_asDonor_returns403() throws Exception {
            mockMvc.perform(get(baseUrl))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = {"HOSPITAL_USER"})
        @DisplayName("Should deny HOSPITAL_USER from listing contracts - 403")
        void getContracts_asHospitalUser_returns403() throws Exception {
            mockMvc.perform(get(baseUrl))
                    .andExpect(status().isForbidden());
        }
    }

    // ==================== GET CONTRACT BY ID ====================

    @Nested
    @DisplayName("GET /api/v1/hospitals/{hospitalId}/contracts/{id}")
    class GetContractById {

        @Test
        @WithMockUser(roles = {"BRANCH_ADMIN"})
        @DisplayName("Should get contract by ID as BRANCH_ADMIN - 200")
        void getContractById_asBranchAdmin_returns200() throws Exception {
            when(contractService.getContractById(contractId)).thenReturn(sampleResponse);

            mockMvc.perform(get(baseUrl + "/{id}", contractId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.contractNumber").value("CTR-001"));
        }

        @Test
        @WithMockUser(roles = {"DONOR"})
        @DisplayName("Should deny DONOR from getting contract by ID - 403")
        void getContractById_asDonor_returns403() throws Exception {
            mockMvc.perform(get(baseUrl + "/{id}", contractId))
                    .andExpect(status().isForbidden());
        }
    }

    // ==================== GET ACTIVE CONTRACTS ====================

    @Nested
    @DisplayName("GET /api/v1/hospitals/{hospitalId}/contracts/active")
    class GetActiveContracts {

        @Test
        @WithMockUser(roles = {"BRANCH_ADMIN"})
        @DisplayName("Should get active contracts as BRANCH_ADMIN - 200")
        void getActiveContracts_asBranchAdmin_returns200() throws Exception {
            when(contractService.getActiveContracts(hospitalId)).thenReturn(List.of(sampleResponse));

            mockMvc.perform(get(baseUrl + "/active"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @WithMockUser(roles = {"DONOR"})
        @DisplayName("Should deny DONOR from getting active contracts - 403")
        void getActiveContracts_asDonor_returns403() throws Exception {
            mockMvc.perform(get(baseUrl + "/active"))
                    .andExpect(status().isForbidden());
        }
    }

    // ==================== UPDATE CONTRACT STATUS ====================

    @Nested
    @DisplayName("PUT /api/v1/hospitals/{hospitalId}/contracts/{id}/status")
    class UpdateContractStatus {

        @Test
        @WithMockUser(roles = {"BRANCH_ADMIN"})
        @DisplayName("Should update contract status as BRANCH_ADMIN - 200")
        void updateContractStatus_asBranchAdmin_returns200() throws Exception {
            when(contractService.updateContractStatus(eq(contractId), eq(ContractStatusEnum.EXPIRED)))
                    .thenReturn(sampleResponse);

            mockMvc.perform(put(baseUrl + "/{id}/status", contractId)
                            .param("status", "EXPIRED"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Contract status updated successfully"));
        }

        @Test
        @WithMockUser(roles = {"DONOR"})
        @DisplayName("Should deny DONOR from updating contract status - 403")
        void updateContractStatus_asDonor_returns403() throws Exception {
            mockMvc.perform(put(baseUrl + "/{id}/status", contractId)
                            .param("status", "EXPIRED"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = {"HOSPITAL_USER"})
        @DisplayName("Should deny HOSPITAL_USER from updating contract status - 403")
        void updateContractStatus_asHospitalUser_returns403() throws Exception {
            mockMvc.perform(put(baseUrl + "/{id}/status", contractId)
                            .param("status", "EXPIRED"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 401 for unauthenticated request")
        void updateContractStatus_unauthenticated_returns401() throws Exception {
            mockMvc.perform(put(baseUrl + "/{id}/status", contractId)
                            .param("status", "EXPIRED"))
                    .andExpect(status().isUnauthorized());
        }
    }
}
