package com.bloodbank.notificationservice.entity;

import com.bloodbank.common.model.BaseEntity;
import com.bloodbank.notificationservice.enums.CampaignStatusEnum;
import com.bloodbank.notificationservice.enums.CampaignTypeEnum;
import com.bloodbank.notificationservice.enums.ChannelEnum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "campaigns")
public class Campaign extends BaseEntity {

    @Column(name = "branch_id")
    private UUID branchId;

    @Column(name = "campaign_code", nullable = false, unique = true, length = 50)
    private String campaignCode;

    @Column(name = "campaign_name", nullable = false, length = 200)
    private String campaignName;

    @Enumerated(EnumType.STRING)
    @Column(name = "campaign_type", nullable = false, length = 30)
    private CampaignTypeEnum campaignType;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 20)
    private ChannelEnum channel;

    @Column(name = "target_audience", length = 50)
    private String targetAudience;

    @Column(name = "target_criteria", columnDefinition = "TEXT")
    private String targetCriteria;

    @Column(name = "template_id")
    private UUID templateId;

    @Column(name = "scheduled_at")
    private Instant scheduledAt;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "total_recipients", nullable = false)
    private int totalRecipients;

    @Column(name = "sent_count", nullable = false)
    private int sentCount;

    @Column(name = "delivered_count", nullable = false)
    private int deliveredCount;

    @Column(name = "failed_count", nullable = false)
    private int failedCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CampaignStatusEnum status = CampaignStatusEnum.DRAFT;

    protected Campaign() {}

    public Campaign(String campaignCode, String campaignName, CampaignTypeEnum campaignType,
                    ChannelEnum channel) {
        this.campaignCode = campaignCode;
        this.campaignName = campaignName;
        this.campaignType = campaignType;
        this.channel = channel;
        this.status = CampaignStatusEnum.DRAFT;
    }

    public UUID getBranchId() { return branchId; }
    public void setBranchId(UUID branchId) { this.branchId = branchId; }

    public String getCampaignCode() { return campaignCode; }
    public void setCampaignCode(String campaignCode) { this.campaignCode = campaignCode; }

    public String getCampaignName() { return campaignName; }
    public void setCampaignName(String campaignName) { this.campaignName = campaignName; }

    public CampaignTypeEnum getCampaignType() { return campaignType; }
    public void setCampaignType(CampaignTypeEnum campaignType) { this.campaignType = campaignType; }

    public ChannelEnum getChannel() { return channel; }
    public void setChannel(ChannelEnum channel) { this.channel = channel; }

    public String getTargetAudience() { return targetAudience; }
    public void setTargetAudience(String targetAudience) { this.targetAudience = targetAudience; }

    public String getTargetCriteria() { return targetCriteria; }
    public void setTargetCriteria(String targetCriteria) { this.targetCriteria = targetCriteria; }

    public UUID getTemplateId() { return templateId; }
    public void setTemplateId(UUID templateId) { this.templateId = templateId; }

    public Instant getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(Instant scheduledAt) { this.scheduledAt = scheduledAt; }

    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }

    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }

    public int getTotalRecipients() { return totalRecipients; }
    public void setTotalRecipients(int totalRecipients) { this.totalRecipients = totalRecipients; }

    public int getSentCount() { return sentCount; }
    public void setSentCount(int sentCount) { this.sentCount = sentCount; }

    public int getDeliveredCount() { return deliveredCount; }
    public void setDeliveredCount(int deliveredCount) { this.deliveredCount = deliveredCount; }

    public int getFailedCount() { return failedCount; }
    public void setFailedCount(int failedCount) { this.failedCount = failedCount; }

    public CampaignStatusEnum getStatus() { return status; }
    public void setStatus(CampaignStatusEnum status) { this.status = status; }
}
