package com.bloodbank.notificationservice.service;

import com.bloodbank.common.exceptions.ResourceNotFoundException;
import com.bloodbank.notificationservice.dto.CampaignCreateRequest;
import com.bloodbank.notificationservice.dto.CampaignResponse;
import com.bloodbank.notificationservice.entity.Campaign;
import com.bloodbank.notificationservice.enums.CampaignStatusEnum;
import com.bloodbank.notificationservice.mapper.CampaignMapper;
import com.bloodbank.notificationservice.repository.CampaignRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class CampaignService {

    private static final Logger log = LoggerFactory.getLogger(CampaignService.class);

    private final CampaignRepository campaignRepository;
    private final CampaignMapper campaignMapper;

    public CampaignService(CampaignRepository campaignRepository,
                           CampaignMapper campaignMapper) {
        this.campaignRepository = campaignRepository;
        this.campaignMapper = campaignMapper;
    }

    @Transactional
    public CampaignResponse create(CampaignCreateRequest request) {
        log.info("Creating campaign: code={}, name={}",
                request.campaignCode(), request.campaignName());
        Campaign campaign = campaignMapper.toEntity(request);
        campaign.setStatus(CampaignStatusEnum.DRAFT);
        campaign = campaignRepository.save(campaign);
        return campaignMapper.toResponse(campaign);
    }

    public CampaignResponse getById(UUID id) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign", "id", id));
        return campaignMapper.toResponse(campaign);
    }

    public List<CampaignResponse> getByStatus(CampaignStatusEnum status) {
        return campaignMapper.toResponseList(campaignRepository.findByStatus(status));
    }

    @Transactional
    public CampaignResponse updateStatus(UUID id, CampaignStatusEnum status) {
        log.info("Updating campaign status: id={}, status={}", id, status);
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign", "id", id));
        campaign.setStatus(status);
        campaign = campaignRepository.save(campaign);
        return campaignMapper.toResponse(campaign);
    }
}
