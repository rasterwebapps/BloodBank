package com.bloodbank.notificationservice.repository;

import com.bloodbank.notificationservice.entity.Campaign;
import com.bloodbank.notificationservice.enums.CampaignStatusEnum;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, UUID>,
                                            JpaSpecificationExecutor<Campaign> {

    Optional<Campaign> findByCampaignCode(String campaignCode);

    List<Campaign> findByStatus(CampaignStatusEnum status);

    List<Campaign> findByBranchId(UUID branchId);
}
