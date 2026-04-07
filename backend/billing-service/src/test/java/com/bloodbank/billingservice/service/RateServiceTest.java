package com.bloodbank.billingservice.service;

import com.bloodbank.common.exceptions.ResourceNotFoundException;
import com.bloodbank.billingservice.dto.RateCreateRequest;
import com.bloodbank.billingservice.dto.RateResponse;
import com.bloodbank.billingservice.entity.RateMaster;
import com.bloodbank.billingservice.mapper.RateMapper;
import com.bloodbank.billingservice.repository.RateRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RateServiceTest {

    @Mock
    private RateRepository rateRepository;

    @Mock
    private RateMapper rateMapper;

    @InjectMocks
    private RateService rateService;

    private UUID rateId;
    private UUID branchId;
    private RateMaster rateMaster;
    private RateResponse rateResponse;
    private RateCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        rateId = UUID.randomUUID();
        branchId = UUID.randomUUID();

        rateMaster = new RateMaster("BLD-001", "Whole Blood Unit", new BigDecimal("150.00"));
        rateMaster.setId(rateId);
        rateMaster.setBranchId(branchId);
        rateMaster.setTaxPercentage(new BigDecimal("5.00"));

        rateResponse = new RateResponse(
                rateId, null, "BLD-001", "Whole Blood Unit",
                new BigDecimal("150.00"), "USD", new BigDecimal("5.00"),
                LocalDate.now(), null, true,
                branchId, LocalDateTime.now(), LocalDateTime.now()
        );

        createRequest = new RateCreateRequest(
                null, "BLD-001", "Whole Blood Unit",
                new BigDecimal("150.00"), "USD", new BigDecimal("5.00"),
                LocalDate.now(), null, branchId
        );
    }

    @Nested
    @DisplayName("createRate")
    class CreateRate {

        @Test
        @DisplayName("should create rate successfully")
        void shouldCreateRateSuccessfully() {
            when(rateMapper.toEntity(createRequest)).thenReturn(rateMaster);
            when(rateRepository.save(any(RateMaster.class))).thenReturn(rateMaster);
            when(rateMapper.toResponse(rateMaster)).thenReturn(rateResponse);

            RateResponse result = rateService.createRate(createRequest);

            assertThat(result).isNotNull();
            assertThat(result.serviceCode()).isEqualTo("BLD-001");
            verify(rateRepository).save(any(RateMaster.class));
        }
    }

    @Nested
    @DisplayName("getRateById")
    class GetRateById {

        @Test
        @DisplayName("should return rate when found")
        void shouldReturnRateWhenFound() {
            when(rateRepository.findById(rateId)).thenReturn(Optional.of(rateMaster));
            when(rateMapper.toResponse(rateMaster)).thenReturn(rateResponse);

            RateResponse result = rateService.getRateById(rateId);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(rateId);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when not found")
        void shouldThrowResourceNotFoundExceptionWhenNotFound() {
            when(rateRepository.findById(rateId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> rateService.getRateById(rateId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getActiveRatesByBranch")
    class GetActiveRatesByBranch {

        @Test
        @DisplayName("should return active rates for branch")
        void shouldReturnActiveRatesForBranch() {
            List<RateMaster> rates = List.of(rateMaster);
            List<RateResponse> responses = List.of(rateResponse);
            when(rateRepository.findByBranchIdAndActiveTrue(branchId)).thenReturn(rates);
            when(rateMapper.toResponseList(rates)).thenReturn(responses);

            List<RateResponse> result = rateService.getActiveRatesByBranch(branchId);

            assertThat(result).hasSize(1);
            verify(rateRepository).findByBranchIdAndActiveTrue(branchId);
        }
    }

    @Nested
    @DisplayName("updateRate")
    class UpdateRate {

        @Test
        @DisplayName("should update rate successfully")
        void shouldUpdateRateSuccessfully() {
            when(rateRepository.findById(rateId)).thenReturn(Optional.of(rateMaster));
            when(rateRepository.save(any(RateMaster.class))).thenReturn(rateMaster);
            when(rateMapper.toResponse(rateMaster)).thenReturn(rateResponse);

            RateResponse result = rateService.updateRate(rateId, createRequest);

            assertThat(result).isNotNull();
            verify(rateRepository).save(any(RateMaster.class));
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when rate not found")
        void shouldThrowResourceNotFoundExceptionWhenRateNotFound() {
            when(rateRepository.findById(rateId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> rateService.updateRate(rateId, createRequest))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("deactivateRate")
    class DeactivateRate {

        @Test
        @DisplayName("should deactivate rate successfully")
        void shouldDeactivateRateSuccessfully() {
            when(rateRepository.findById(rateId)).thenReturn(Optional.of(rateMaster));
            when(rateRepository.save(any(RateMaster.class))).thenReturn(rateMaster);

            rateService.deactivateRate(rateId);

            assertThat(rateMaster.isActive()).isFalse();
            verify(rateRepository).save(rateMaster);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when rate not found")
        void shouldThrowResourceNotFoundExceptionWhenRateNotFound() {
            when(rateRepository.findById(rateId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> rateService.deactivateRate(rateId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
