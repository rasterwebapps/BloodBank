package com.bloodbank.donorservice.entity;

import com.bloodbank.common.model.BranchScopedEntity;
import com.bloodbank.donorservice.enums.LoyaltyTierEnum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "donor_loyalty")
@FilterDef(name = "branchFilter", parameters = @ParamDef(name = "branchId", type = UUID.class))
@Filter(name = "branchFilter", condition = "branch_id = :branchId")
public class DonorLoyalty extends BranchScopedEntity {

    @Column(name = "donor_id", nullable = false, unique = true)
    private UUID donorId;

    @Column(name = "points_earned", nullable = false)
    private int pointsEarned;

    @Column(name = "points_redeemed", nullable = false)
    private int pointsRedeemed;

    @Column(name = "points_balance", nullable = false)
    private int pointsBalance;

    @Enumerated(EnumType.STRING)
    @Column(name = "tier", nullable = false, length = 20)
    private LoyaltyTierEnum tier;

    @Column(name = "last_activity_date")
    private Instant lastActivityDate;

    protected DonorLoyalty() {}

    public UUID getDonorId() { return donorId; }
    public void setDonorId(UUID donorId) { this.donorId = donorId; }

    public int getPointsEarned() { return pointsEarned; }
    public void setPointsEarned(int pointsEarned) { this.pointsEarned = pointsEarned; }

    public int getPointsRedeemed() { return pointsRedeemed; }
    public void setPointsRedeemed(int pointsRedeemed) { this.pointsRedeemed = pointsRedeemed; }

    public int getPointsBalance() { return pointsBalance; }
    public void setPointsBalance(int pointsBalance) { this.pointsBalance = pointsBalance; }

    public LoyaltyTierEnum getTier() { return tier; }
    public void setTier(LoyaltyTierEnum tier) { this.tier = tier; }

    public Instant getLastActivityDate() { return lastActivityDate; }
    public void setLastActivityDate(Instant lastActivityDate) { this.lastActivityDate = lastActivityDate; }
}
