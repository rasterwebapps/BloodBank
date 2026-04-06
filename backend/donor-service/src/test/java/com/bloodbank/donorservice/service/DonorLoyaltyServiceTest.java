package com.bloodbank.donorservice.service;

import com.bloodbank.common.exceptions.BusinessException;
import com.bloodbank.common.exceptions.ResourceNotFoundException;
import com.bloodbank.donorservice.dto.DonorLoyaltyResponse;
import com.bloodbank.donorservice.entity.Donor;
import com.bloodbank.donorservice.entity.DonorLoyalty;
import com.bloodbank.donorservice.enums.LoyaltyTierEnum;
import com.bloodbank.donorservice.mapper.DonorLoyaltyMapper;
import com.bloodbank.donorservice.repository.DonorLoyaltyRepository;
import com.bloodbank.donorservice.repository.DonorRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DonorLoyaltyServiceTest {

    @Mock
    private DonorLoyaltyRepository loyaltyRepository;

    @Mock
    private DonorRepository donorRepository;

    @Mock
    private DonorLoyaltyMapper loyaltyMapper;

    @InjectMocks
    private DonorLoyaltyService donorLoyaltyService;

    private UUID donorId;
    private UUID branchId;
    private Donor donor;
    private DonorLoyalty loyalty;
    private DonorLoyaltyResponse loyaltyResponse;

    @BeforeEach
    void setUp() {
        donorId = UUID.randomUUID();
        branchId = UUID.randomUUID();

        donor = new Donor();
        donor.setId(donorId);
        donor.setFirstName("John");
        donor.setLastName("Doe");
        donor.setBranchId(branchId);

        loyalty = new DonorLoyalty();
        loyalty.setId(UUID.randomUUID());
        loyalty.setDonorId(donorId);
        loyalty.setPointsEarned(50);
        loyalty.setPointsRedeemed(10);
        loyalty.setPointsBalance(40);
        loyalty.setTier(LoyaltyTierEnum.BRONZE);
        loyalty.setBranchId(branchId);

        loyaltyResponse = new DonorLoyaltyResponse(
                loyalty.getId(), donorId, 50, 10, 40,
                LoyaltyTierEnum.BRONZE, null,
                branchId, LocalDateTime.now()
        );
    }

    @Nested
    @DisplayName("getOrCreateLoyalty")
    class GetOrCreateLoyalty {

        @Test
        @DisplayName("should return existing loyalty record")
        void shouldReturnExistingLoyalty() {
            when(donorRepository.findById(donorId)).thenReturn(Optional.of(donor));
            when(loyaltyRepository.findByDonorId(donorId)).thenReturn(Optional.of(loyalty));
            when(loyaltyMapper.toResponse(loyalty)).thenReturn(loyaltyResponse);

            DonorLoyaltyResponse result = donorLoyaltyService.getOrCreateLoyalty(donorId);

            assertThat(result).isNotNull();
            assertThat(result.donorId()).isEqualTo(donorId);
            assertThat(result.pointsBalance()).isEqualTo(40);
            assertThat(result.tier()).isEqualTo(LoyaltyTierEnum.BRONZE);
        }

        @Test
        @DisplayName("should create new loyalty record when none exists")
        void shouldCreateNewLoyalty() {
            DonorLoyalty newLoyalty = new DonorLoyalty();
            newLoyalty.setId(UUID.randomUUID());
            newLoyalty.setDonorId(donorId);
            newLoyalty.setPointsEarned(0);
            newLoyalty.setPointsRedeemed(0);
            newLoyalty.setPointsBalance(0);
            newLoyalty.setTier(LoyaltyTierEnum.BRONZE);
            newLoyalty.setBranchId(branchId);

            DonorLoyaltyResponse newResponse = new DonorLoyaltyResponse(
                    newLoyalty.getId(), donorId, 0, 0, 0,
                    LoyaltyTierEnum.BRONZE, null,
                    branchId, LocalDateTime.now()
            );

            when(donorRepository.findById(donorId)).thenReturn(Optional.of(donor));
            when(loyaltyRepository.findByDonorId(donorId)).thenReturn(Optional.empty());
            when(loyaltyRepository.save(any(DonorLoyalty.class))).thenReturn(newLoyalty);
            when(loyaltyMapper.toResponse(newLoyalty)).thenReturn(newResponse);

            DonorLoyaltyResponse result = donorLoyaltyService.getOrCreateLoyalty(donorId);

            assertThat(result).isNotNull();
            assertThat(result.pointsBalance()).isZero();
            assertThat(result.tier()).isEqualTo(LoyaltyTierEnum.BRONZE);
            verify(loyaltyRepository).save(any(DonorLoyalty.class));
        }
    }

    @Nested
    @DisplayName("awardPoints")
    class AwardPoints {

        @Test
        @DisplayName("should award points successfully")
        void shouldAwardPointsSuccessfully() {
            DonorLoyalty updatedLoyalty = new DonorLoyalty();
            updatedLoyalty.setId(loyalty.getId());
            updatedLoyalty.setDonorId(donorId);
            updatedLoyalty.setPointsEarned(100);
            updatedLoyalty.setPointsRedeemed(10);
            updatedLoyalty.setPointsBalance(90);
            updatedLoyalty.setTier(LoyaltyTierEnum.SILVER);
            updatedLoyalty.setBranchId(branchId);

            DonorLoyaltyResponse updatedResponse = new DonorLoyaltyResponse(
                    loyalty.getId(), donorId, 100, 10, 90,
                    LoyaltyTierEnum.SILVER, LocalDateTime.now(),
                    branchId, LocalDateTime.now()
            );

            when(donorRepository.findById(donorId)).thenReturn(Optional.of(donor));
            when(loyaltyRepository.findByDonorId(donorId)).thenReturn(Optional.of(loyalty));
            when(loyaltyRepository.save(any(DonorLoyalty.class))).thenReturn(updatedLoyalty);
            when(loyaltyMapper.toResponse(updatedLoyalty)).thenReturn(updatedResponse);

            DonorLoyaltyResponse result = donorLoyaltyService.awardPoints(donorId, 50);

            assertThat(result).isNotNull();
            assertThat(result.pointsEarned()).isEqualTo(100);
            assertThat(result.pointsBalance()).isEqualTo(90);
            verify(loyaltyRepository).save(any(DonorLoyalty.class));
        }

        @Test
        @DisplayName("should upgrade tier to SILVER at 100 points")
        void shouldUpgradeToSilver() {
            loyalty.setPointsEarned(80);
            loyalty.setPointsBalance(80);

            DonorLoyalty savedLoyalty = new DonorLoyalty();
            savedLoyalty.setId(loyalty.getId());
            savedLoyalty.setDonorId(donorId);
            savedLoyalty.setPointsEarned(100);
            savedLoyalty.setPointsBalance(100);
            savedLoyalty.setTier(LoyaltyTierEnum.SILVER);
            savedLoyalty.setBranchId(branchId);

            DonorLoyaltyResponse silverResponse = new DonorLoyaltyResponse(
                    loyalty.getId(), donorId, 100, 0, 100,
                    LoyaltyTierEnum.SILVER, LocalDateTime.now(),
                    branchId, LocalDateTime.now()
            );

            when(donorRepository.findById(donorId)).thenReturn(Optional.of(donor));
            when(loyaltyRepository.findByDonorId(donorId)).thenReturn(Optional.of(loyalty));
            when(loyaltyRepository.save(any(DonorLoyalty.class))).thenReturn(savedLoyalty);
            when(loyaltyMapper.toResponse(savedLoyalty)).thenReturn(silverResponse);

            DonorLoyaltyResponse result = donorLoyaltyService.awardPoints(donorId, 20);

            assertThat(result.tier()).isEqualTo(LoyaltyTierEnum.SILVER);
            assertThat(result.pointsEarned()).isEqualTo(100);
        }

        @Test
        @DisplayName("should upgrade tier to GOLD at 500 points")
        void shouldUpgradeToGold() {
            loyalty.setPointsEarned(450);
            loyalty.setPointsBalance(450);

            DonorLoyalty savedLoyalty = new DonorLoyalty();
            savedLoyalty.setId(loyalty.getId());
            savedLoyalty.setDonorId(donorId);
            savedLoyalty.setPointsEarned(500);
            savedLoyalty.setPointsBalance(500);
            savedLoyalty.setTier(LoyaltyTierEnum.GOLD);
            savedLoyalty.setBranchId(branchId);

            DonorLoyaltyResponse goldResponse = new DonorLoyaltyResponse(
                    loyalty.getId(), donorId, 500, 0, 500,
                    LoyaltyTierEnum.GOLD, LocalDateTime.now(),
                    branchId, LocalDateTime.now()
            );

            when(donorRepository.findById(donorId)).thenReturn(Optional.of(donor));
            when(loyaltyRepository.findByDonorId(donorId)).thenReturn(Optional.of(loyalty));
            when(loyaltyRepository.save(any(DonorLoyalty.class))).thenReturn(savedLoyalty);
            when(loyaltyMapper.toResponse(savedLoyalty)).thenReturn(goldResponse);

            DonorLoyaltyResponse result = donorLoyaltyService.awardPoints(donorId, 50);

            assertThat(result.tier()).isEqualTo(LoyaltyTierEnum.GOLD);
            assertThat(result.pointsEarned()).isEqualTo(500);
        }

        @Test
        @DisplayName("should upgrade tier to PLATINUM at 1000 points")
        void shouldUpgradeToPlatinum() {
            loyalty.setPointsEarned(950);
            loyalty.setPointsBalance(950);

            DonorLoyalty savedLoyalty = new DonorLoyalty();
            savedLoyalty.setId(loyalty.getId());
            savedLoyalty.setDonorId(donorId);
            savedLoyalty.setPointsEarned(1050);
            savedLoyalty.setPointsBalance(1050);
            savedLoyalty.setTier(LoyaltyTierEnum.PLATINUM);
            savedLoyalty.setBranchId(branchId);

            DonorLoyaltyResponse platinumResponse = new DonorLoyaltyResponse(
                    loyalty.getId(), donorId, 1050, 0, 1050,
                    LoyaltyTierEnum.PLATINUM, LocalDateTime.now(),
                    branchId, LocalDateTime.now()
            );

            when(donorRepository.findById(donorId)).thenReturn(Optional.of(donor));
            when(loyaltyRepository.findByDonorId(donorId)).thenReturn(Optional.of(loyalty));
            when(loyaltyRepository.save(any(DonorLoyalty.class))).thenReturn(savedLoyalty);
            when(loyaltyMapper.toResponse(savedLoyalty)).thenReturn(platinumResponse);

            DonorLoyaltyResponse result = donorLoyaltyService.awardPoints(donorId, 100);

            assertThat(result.tier()).isEqualTo(LoyaltyTierEnum.PLATINUM);
            assertThat(result.pointsEarned()).isEqualTo(1050);
        }
    }

    @Nested
    @DisplayName("redeemPoints")
    class RedeemPoints {

        @Test
        @DisplayName("should redeem points successfully")
        void shouldRedeemPointsSuccessfully() {
            DonorLoyalty redeemedLoyalty = new DonorLoyalty();
            redeemedLoyalty.setId(loyalty.getId());
            redeemedLoyalty.setDonorId(donorId);
            redeemedLoyalty.setPointsEarned(50);
            redeemedLoyalty.setPointsRedeemed(30);
            redeemedLoyalty.setPointsBalance(20);
            redeemedLoyalty.setTier(LoyaltyTierEnum.BRONZE);
            redeemedLoyalty.setBranchId(branchId);

            DonorLoyaltyResponse redeemedResponse = new DonorLoyaltyResponse(
                    loyalty.getId(), donorId, 50, 30, 20,
                    LoyaltyTierEnum.BRONZE, LocalDateTime.now(),
                    branchId, LocalDateTime.now()
            );

            when(loyaltyRepository.findByDonorId(donorId)).thenReturn(Optional.of(loyalty));
            when(loyaltyRepository.save(any(DonorLoyalty.class))).thenReturn(redeemedLoyalty);
            when(loyaltyMapper.toResponse(redeemedLoyalty)).thenReturn(redeemedResponse);

            DonorLoyaltyResponse result = donorLoyaltyService.redeemPoints(donorId, 20);

            assertThat(result).isNotNull();
            assertThat(result.pointsBalance()).isEqualTo(20);
            assertThat(result.pointsRedeemed()).isEqualTo(30);
            verify(loyaltyRepository).save(any(DonorLoyalty.class));
        }

        @Test
        @DisplayName("should throw BusinessException when insufficient points")
        void shouldThrowWhenInsufficientPoints() {
            when(loyaltyRepository.findByDonorId(donorId)).thenReturn(Optional.of(loyalty));

            assertThatThrownBy(() -> donorLoyaltyService.redeemPoints(donorId, 100))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Insufficient loyalty points");
        }
    }

    @Nested
    @DisplayName("getLoyalty")
    class GetLoyalty {

        @Test
        @DisplayName("should return loyalty when found")
        void shouldReturnLoyaltyWhenFound() {
            when(loyaltyRepository.findByDonorId(donorId)).thenReturn(Optional.of(loyalty));
            when(loyaltyMapper.toResponse(loyalty)).thenReturn(loyaltyResponse);

            DonorLoyaltyResponse result = donorLoyaltyService.getLoyalty(donorId);

            assertThat(result).isNotNull();
            assertThat(result.donorId()).isEqualTo(donorId);
            assertThat(result.tier()).isEqualTo(LoyaltyTierEnum.BRONZE);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            when(loyaltyRepository.findByDonorId(donorId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> donorLoyaltyService.getLoyalty(donorId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
