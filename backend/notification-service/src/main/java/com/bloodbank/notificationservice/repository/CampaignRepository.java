package com.bloodbank.notificationservice.repository;

import com.bloodbank.notificationservice.entity.Campaign;
import com.bloodbank.notificationservice.enums.CampaignStatusEnum;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.QueryHint;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, UUID>,
                                            JpaSpecificationExecutor<Campaign> {

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    Optional<Campaign> findByCampaignCode(String campaignCode);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<Campaign> findByStatus(CampaignStatusEnum status);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<Campaign> findByBranchId(UUID branchId);
}
