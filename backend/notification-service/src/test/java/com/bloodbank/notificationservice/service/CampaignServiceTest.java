package com.bloodbank.notificationservice.service;

import com.bloodbank.common.exceptions.ResourceNotFoundException;
import com.bloodbank.notificationservice.dto.CampaignCreateRequest;
import com.bloodbank.notificationservice.dto.CampaignResponse;
import com.bloodbank.notificationservice.entity.Campaign;
import com.bloodbank.notificationservice.enums.CampaignStatusEnum;
import com.bloodbank.notificationservice.enums.CampaignTypeEnum;
import com.bloodbank.notificationservice.enums.ChannelEnum;
import com.bloodbank.notificationservice.mapper.CampaignMapper;
import com.bloodbank.notificationservice.repository.CampaignRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
class CampaignServiceTest {

    @Mock
    private CampaignRepository campaignRepository;

    @Mock
    private CampaignMapper campaignMapper;

    @InjectMocks
    private CampaignService campaignService;

    private UUID campaignId;
    private Campaign campaign;
    private CampaignResponse campaignResponse;
    private CampaignCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        campaignId = UUID.randomUUID();

        campaign = new Campaign("SUMMER_DRIVE_2024", "Summer Blood Drive 2024",
                CampaignTypeEnum.DONATION_DRIVE, ChannelEnum.EMAIL);
        campaign.setId(campaignId);
        campaign.setStatus(CampaignStatusEnum.DRAFT);

        campaignResponse = new CampaignResponse(
                campaignId, null, "SUMMER_DRIVE_2024", "Summer Blood Drive 2024",
                CampaignTypeEnum.DONATION_DRIVE, ChannelEnum.EMAIL, null, null,
                null, null, null, null, 0, 0, 0, 0,
                CampaignStatusEnum.DRAFT, LocalDateTime.now(), LocalDateTime.now()
        );

        createRequest = new CampaignCreateRequest(
                null, "SUMMER_DRIVE_2024", "Summer Blood Drive 2024",
                CampaignTypeEnum.DONATION_DRIVE, ChannelEnum.EMAIL,
                null, null, null, null
        );
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("should create campaign successfully")
        void shouldCreateCampaignSuccessfully() {
            when(campaignMapper.toEntity(createRequest)).thenReturn(campaign);
            when(campaignRepository.save(any(Campaign.class))).thenReturn(campaign);
            when(campaignMapper.toResponse(campaign)).thenReturn(campaignResponse);

            CampaignResponse result = campaignService.create(createRequest);

            assertThat(result).isNotNull();
            assertThat(result.campaignCode()).isEqualTo("SUMMER_DRIVE_2024");
            assertThat(campaign.getStatus()).isEqualTo(CampaignStatusEnum.DRAFT);
            verify(campaignRepository).save(any(Campaign.class));
        }
    }

    @Nested
    @DisplayName("getById")
    class GetById {

        @Test
        @DisplayName("should return campaign when found")
        void shouldReturnCampaignWhenFound() {
            when(campaignRepository.findById(campaignId)).thenReturn(Optional.of(campaign));
            when(campaignMapper.toResponse(campaign)).thenReturn(campaignResponse);

            CampaignResponse result = campaignService.getById(campaignId);

            assertThat(result).isNotNull();
            assertThat(result.campaignCode()).isEqualTo("SUMMER_DRIVE_2024");
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when not found")
        void shouldThrowResourceNotFoundWhenNotFound() {
            when(campaignRepository.findById(campaignId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> campaignService.getById(campaignId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getByStatus")
    class GetByStatus {

        @Test
        @DisplayName("should return campaigns by status")
        void shouldReturnCampaignsByStatus() {
            List<Campaign> campaigns = List.of(campaign);
            List<CampaignResponse> responses = List.of(campaignResponse);
            when(campaignRepository.findByStatus(CampaignStatusEnum.DRAFT)).thenReturn(campaigns);
            when(campaignMapper.toResponseList(campaigns)).thenReturn(responses);

            List<CampaignResponse> result = campaignService.getByStatus(CampaignStatusEnum.DRAFT);

            assertThat(result).hasSize(1);
            verify(campaignRepository).findByStatus(CampaignStatusEnum.DRAFT);
        }
    }

    @Nested
    @DisplayName("updateStatus")
    class UpdateStatus {

        @Test
        @DisplayName("should update campaign status")
        void shouldUpdateCampaignStatus() {
            when(campaignRepository.findById(campaignId)).thenReturn(Optional.of(campaign));
            when(campaignRepository.save(any(Campaign.class))).thenReturn(campaign);
            when(campaignMapper.toResponse(campaign)).thenReturn(campaignResponse);

            CampaignResponse result = campaignService.updateStatus(campaignId, CampaignStatusEnum.IN_PROGRESS);

            assertThat(result).isNotNull();
            assertThat(campaign.getStatus()).isEqualTo(CampaignStatusEnum.IN_PROGRESS);
            verify(campaignRepository).save(campaign);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when not found")
        void shouldThrowResourceNotFoundWhenNotFound() {
            when(campaignRepository.findById(campaignId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> campaignService.updateStatus(campaignId, CampaignStatusEnum.IN_PROGRESS))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
