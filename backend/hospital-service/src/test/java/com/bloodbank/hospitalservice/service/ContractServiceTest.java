package com.bloodbank.hospitalservice.service;

import com.bloodbank.common.dto.PagedResponse;
import com.bloodbank.common.exceptions.ConflictException;
import com.bloodbank.common.exceptions.ResourceNotFoundException;
import com.bloodbank.hospitalservice.dto.HospitalContractCreateRequest;
import com.bloodbank.hospitalservice.dto.HospitalContractResponse;
import com.bloodbank.hospitalservice.entity.Hospital;
import com.bloodbank.hospitalservice.entity.HospitalContract;
import com.bloodbank.hospitalservice.enums.ContractStatusEnum;
import com.bloodbank.hospitalservice.mapper.HospitalContractMapper;
import com.bloodbank.hospitalservice.repository.HospitalContractRepository;
import com.bloodbank.hospitalservice.repository.HospitalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContractServiceTest {

    @Mock
    private HospitalContractRepository contractRepository;

    @Mock
    private HospitalRepository hospitalRepository;

    @Mock
    private HospitalContractMapper contractMapper;

    @InjectMocks
    private ContractService contractService;

    private UUID contractId;
    private UUID hospitalId;
    private UUID branchId;
    private HospitalContract contract;
    private HospitalContractResponse contractResponse;
    private HospitalContractCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        contractId = UUID.randomUUID();
        hospitalId = UUID.randomUUID();
        branchId = UUID.randomUUID();

        contract = new HospitalContract();
        contract.setHospitalId(hospitalId);
        contract.setContractNumber("CTR-001");
        contract.setStartDate(LocalDate.of(2024, 1, 1));
        contract.setEndDate(LocalDate.of(2025, 12, 31));
        contract.setDiscountPercentage(new BigDecimal("10.00"));
        contract.setPaymentTermsDays(30);
        contract.setCreditLimit(new BigDecimal("100000.00"));
        contract.setAutoRenew(true);
        contract.setStatus(ContractStatusEnum.ACTIVE);
        contract.setNotes("Standard contract");

        contractResponse = new HospitalContractResponse(
                contractId, hospitalId, "CTR-001",
                LocalDate.of(2024, 1, 1), LocalDate.of(2025, 12, 31),
                new BigDecimal("10.00"), 30, new BigDecimal("100000.00"),
                true, ContractStatusEnum.ACTIVE, null, "Standard contract",
                branchId, LocalDateTime.now(), LocalDateTime.now()
        );

        createRequest = new HospitalContractCreateRequest(
                hospitalId, "CTR-001",
                LocalDate.of(2024, 1, 1), LocalDate.of(2025, 12, 31),
                new BigDecimal("10.00"), 30, new BigDecimal("100000.00"),
                true, null, "Standard contract", branchId
        );
    }

    @Nested
    @DisplayName("createContract")
    class CreateContract {

        @Test
        @DisplayName("Should create contract successfully")
        void shouldCreateContractSuccessfully() {
            when(hospitalRepository.findById(hospitalId)).thenReturn(Optional.of(new Hospital()));
            when(contractRepository.existsByContractNumber("CTR-001")).thenReturn(false);
            when(contractMapper.toEntity(createRequest)).thenReturn(contract);
            when(contractRepository.save(any(HospitalContract.class))).thenReturn(contract);
            when(contractMapper.toResponse(contract)).thenReturn(contractResponse);

            HospitalContractResponse result = contractService.createContract(createRequest);

            assertThat(result).isNotNull();
            assertThat(result.contractNumber()).isEqualTo("CTR-001");
            assertThat(result.status()).isEqualTo(ContractStatusEnum.ACTIVE);
            verify(contractRepository).save(any(HospitalContract.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when hospital not found")
        void shouldThrowWhenHospitalNotFound() {
            when(hospitalRepository.findById(hospitalId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> contractService.createContract(createRequest))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(contractRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw ConflictException when contract number already exists")
        void shouldThrowWhenContractNumberExists() {
            when(hospitalRepository.findById(hospitalId)).thenReturn(Optional.of(new Hospital()));
            when(contractRepository.existsByContractNumber("CTR-001")).thenReturn(true);

            assertThatThrownBy(() -> contractService.createContract(createRequest))
                    .isInstanceOf(ConflictException.class);

            verify(contractRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should set ACTIVE status and branchId on creation")
        void shouldSetStatusAndBranchId() {
            HospitalContract capturedContract = new HospitalContract();
            when(hospitalRepository.findById(hospitalId)).thenReturn(Optional.of(new Hospital()));
            when(contractRepository.existsByContractNumber("CTR-001")).thenReturn(false);
            when(contractMapper.toEntity(createRequest)).thenReturn(capturedContract);
            when(contractRepository.save(any(HospitalContract.class))).thenAnswer(inv -> inv.getArgument(0));
            when(contractMapper.toResponse(any(HospitalContract.class))).thenReturn(contractResponse);

            contractService.createContract(createRequest);

            assertThat(capturedContract.getStatus()).isEqualTo(ContractStatusEnum.ACTIVE);
            assertThat(capturedContract.getBranchId()).isEqualTo(branchId);
        }
    }

    @Nested
    @DisplayName("getContractById")
    class GetContractById {

        @Test
        @DisplayName("Should return contract when found")
        void shouldReturnContractWhenFound() {
            when(contractRepository.findById(contractId)).thenReturn(Optional.of(contract));
            when(contractMapper.toResponse(contract)).thenReturn(contractResponse);

            HospitalContractResponse result = contractService.getContractById(contractId);

            assertThat(result).isNotNull();
            assertThat(result.contractNumber()).isEqualTo("CTR-001");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            when(contractRepository.findById(contractId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> contractService.getContractById(contractId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getContractsByHospitalId")
    class GetContractsByHospitalId {

        @Test
        @DisplayName("Should return paged contracts for hospital")
        void shouldReturnPagedContracts() {
            Pageable pageable = PageRequest.of(0, 20);
            Page<HospitalContract> page = new PageImpl<>(List.of(contract), pageable, 1);
            when(contractRepository.findByHospitalId(hospitalId, pageable)).thenReturn(page);
            when(contractMapper.toResponseList(List.of(contract))).thenReturn(List.of(contractResponse));

            PagedResponse<HospitalContractResponse> result = contractService.getContractsByHospitalId(hospitalId, pageable);

            assertThat(result.content()).hasSize(1);
            assertThat(result.totalElements()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("getActiveContracts")
    class GetActiveContracts {

        @Test
        @DisplayName("Should return active contracts for hospital")
        void shouldReturnActiveContracts() {
            when(contractRepository.findByHospitalIdAndStatus(hospitalId, ContractStatusEnum.ACTIVE))
                    .thenReturn(List.of(contract));
            when(contractMapper.toResponseList(List.of(contract))).thenReturn(List.of(contractResponse));

            List<HospitalContractResponse> result = contractService.getActiveContracts(hospitalId);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).status()).isEqualTo(ContractStatusEnum.ACTIVE);
        }

        @Test
        @DisplayName("Should return empty list when no active contracts")
        void shouldReturnEmptyList() {
            when(contractRepository.findByHospitalIdAndStatus(hospitalId, ContractStatusEnum.ACTIVE))
                    .thenReturn(List.of());
            when(contractMapper.toResponseList(List.of())).thenReturn(List.of());

            List<HospitalContractResponse> result = contractService.getActiveContracts(hospitalId);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("updateContractStatus")
    class UpdateContractStatus {

        @Test
        @DisplayName("Should update contract status successfully")
        void shouldUpdateStatusSuccessfully() {
            when(contractRepository.findById(contractId)).thenReturn(Optional.of(contract));
            when(contractRepository.save(any(HospitalContract.class))).thenReturn(contract);
            when(contractMapper.toResponse(contract)).thenReturn(contractResponse);

            HospitalContractResponse result = contractService.updateContractStatus(contractId, ContractStatusEnum.EXPIRED);

            assertThat(result).isNotNull();
            verify(contractRepository).save(contract);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when not found for status update")
        void shouldThrowWhenNotFoundForStatusUpdate() {
            when(contractRepository.findById(contractId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> contractService.updateContractStatus(contractId, ContractStatusEnum.EXPIRED))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should set new status on entity")
        void shouldSetNewStatus() {
            when(contractRepository.findById(contractId)).thenReturn(Optional.of(contract));
            when(contractRepository.save(any(HospitalContract.class))).thenReturn(contract);
            when(contractMapper.toResponse(contract)).thenReturn(contractResponse);

            contractService.updateContractStatus(contractId, ContractStatusEnum.TERMINATED);

            assertThat(contract.getStatus()).isEqualTo(ContractStatusEnum.TERMINATED);
        }
    }
}
