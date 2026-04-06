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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class DonorLoyaltyService {

    private static final Logger log = LoggerFactory.getLogger(DonorLoyaltyService.class);

    private static final int SILVER_THRESHOLD = 100;
    private static final int GOLD_THRESHOLD = 500;
    private static final int PLATINUM_THRESHOLD = 1000;

    private final DonorLoyaltyRepository loyaltyRepository;
    private final DonorRepository donorRepository;
    private final DonorLoyaltyMapper loyaltyMapper;

    public DonorLoyaltyService(DonorLoyaltyRepository loyaltyRepository,
                               DonorRepository donorRepository,
                               DonorLoyaltyMapper loyaltyMapper) {
        this.loyaltyRepository = loyaltyRepository;
        this.donorRepository = donorRepository;
        this.loyaltyMapper = loyaltyMapper;
    }

    @Transactional
    public DonorLoyaltyResponse getOrCreateLoyalty(UUID donorId) {
        log.info("Getting or creating loyalty record for donor: {}", donorId);

        Donor donor = donorRepository.findById(donorId)
                .orElseThrow(() -> new ResourceNotFoundException("Donor", "id", donorId));

        DonorLoyalty loyalty = loyaltyRepository.findByDonorId(donorId)
                .orElseGet(() -> {
                    DonorLoyalty newLoyalty = new DonorLoyalty();
                    newLoyalty.setDonorId(donorId);
                    newLoyalty.setPointsEarned(0);
                    newLoyalty.setPointsRedeemed(0);
                    newLoyalty.setPointsBalance(0);
                    newLoyalty.setTier(LoyaltyTierEnum.BRONZE);
                    newLoyalty.setBranchId(donor.getBranchId());
                    return loyaltyRepository.save(newLoyalty);
                });

        return loyaltyMapper.toResponse(loyalty);
    }

    @Transactional
    public DonorLoyaltyResponse awardPoints(UUID donorId, int points) {
        log.info("Awarding {} points to donor: {}", points, donorId);

        Donor donor = donorRepository.findById(donorId)
                .orElseThrow(() -> new ResourceNotFoundException("Donor", "id", donorId));

        DonorLoyalty loyalty = loyaltyRepository.findByDonorId(donorId)
                .orElseGet(() -> {
                    DonorLoyalty newLoyalty = new DonorLoyalty();
                    newLoyalty.setDonorId(donorId);
                    newLoyalty.setPointsEarned(0);
                    newLoyalty.setPointsRedeemed(0);
                    newLoyalty.setPointsBalance(0);
                    newLoyalty.setTier(LoyaltyTierEnum.BRONZE);
                    newLoyalty.setBranchId(donor.getBranchId());
                    return loyaltyRepository.save(newLoyalty);
                });

        loyalty.setPointsEarned(loyalty.getPointsEarned() + points);
        loyalty.setPointsBalance(loyalty.getPointsBalance() + points);
        loyalty.setTier(calculateTier(loyalty.getPointsEarned()));
        loyalty.setLastActivityDate(Instant.now());

        loyalty = loyaltyRepository.save(loyalty);
        log.info("Donor {} now has {} points, tier: {}", donorId, loyalty.getPointsBalance(), loyalty.getTier());
        return loyaltyMapper.toResponse(loyalty);
    }

    @Transactional
    public DonorLoyaltyResponse redeemPoints(UUID donorId, int points) {
        log.info("Redeeming {} points for donor: {}", points, donorId);

        DonorLoyalty loyalty = loyaltyRepository.findByDonorId(donorId)
                .orElseThrow(() -> new ResourceNotFoundException("DonorLoyalty", "donorId", donorId));

        if (loyalty.getPointsBalance() < points) {
            throw new BusinessException("Insufficient loyalty points", "INSUFFICIENT_POINTS");
        }

        loyalty.setPointsBalance(loyalty.getPointsBalance() - points);
        loyalty.setPointsRedeemed(loyalty.getPointsRedeemed() + points);
        loyalty.setLastActivityDate(Instant.now());

        loyalty = loyaltyRepository.save(loyalty);
        log.info("Donor {} redeemed {} points, remaining balance: {}", donorId, points, loyalty.getPointsBalance());
        return loyaltyMapper.toResponse(loyalty);
    }

    public DonorLoyaltyResponse getLoyalty(UUID donorId) {
        log.debug("Fetching loyalty for donor: {}", donorId);
        DonorLoyalty loyalty = loyaltyRepository.findByDonorId(donorId)
                .orElseThrow(() -> new ResourceNotFoundException("DonorLoyalty", "donorId", donorId));
        return loyaltyMapper.toResponse(loyalty);
    }

    private LoyaltyTierEnum calculateTier(int pointsEarned) {
        if (pointsEarned >= PLATINUM_THRESHOLD) {
            return LoyaltyTierEnum.PLATINUM;
        } else if (pointsEarned >= GOLD_THRESHOLD) {
            return LoyaltyTierEnum.GOLD;
        } else if (pointsEarned >= SILVER_THRESHOLD) {
            return LoyaltyTierEnum.SILVER;
        }
        return LoyaltyTierEnum.BRONZE;
    }
}
