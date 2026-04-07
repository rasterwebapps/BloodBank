package com.bloodbank.notificationservice.entity;

import com.bloodbank.common.model.BaseEntity;
import com.bloodbank.notificationservice.enums.ChannelEnum;
import com.bloodbank.notificationservice.enums.NotificationStatusEnum;
import com.bloodbank.notificationservice.enums.RecipientTypeEnum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notifications")
public class Notification extends BaseEntity {

    @Column(name = "branch_id")
    private UUID branchId;

    @Column(name = "template_id")
    private UUID templateId;

    @Enumerated(EnumType.STRING)
    @Column(name = "recipient_type", length = 20)
    private RecipientTypeEnum recipientType;

    @Column(name = "recipient_id")
    private UUID recipientId;

    @Column(name = "recipient_email", length = 255)
    private String recipientEmail;

    @Column(name = "recipient_phone", length = 20)
    private String recipientPhone;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 20)
    private ChannelEnum channel;

    @Column(name = "subject", length = 500)
    private String subject;

    @Column(name = "body", columnDefinition = "TEXT")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private NotificationStatusEnum status = NotificationStatusEnum.PENDING;

    @Column(name = "sent_at")
    private Instant sentAt;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @Column(name = "read_at")
    private Instant readAt;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "external_reference", length = 255)
    private String externalReference;

    protected Notification() {}

    public Notification(ChannelEnum channel, String subject, String body, RecipientTypeEnum recipientType,
                        UUID recipientId) {
        this.channel = channel;
        this.subject = subject;
        this.body = body;
        this.recipientType = recipientType;
        this.recipientId = recipientId;
        this.status = NotificationStatusEnum.PENDING;
        this.retryCount = 0;
    }

    public UUID getBranchId() { return branchId; }
    public void setBranchId(UUID branchId) { this.branchId = branchId; }

    public UUID getTemplateId() { return templateId; }
    public void setTemplateId(UUID templateId) { this.templateId = templateId; }

    public RecipientTypeEnum getRecipientType() { return recipientType; }
    public void setRecipientType(RecipientTypeEnum recipientType) { this.recipientType = recipientType; }

    public UUID getRecipientId() { return recipientId; }
    public void setRecipientId(UUID recipientId) { this.recipientId = recipientId; }

    public String getRecipientEmail() { return recipientEmail; }
    public void setRecipientEmail(String recipientEmail) { this.recipientEmail = recipientEmail; }

    public String getRecipientPhone() { return recipientPhone; }
    public void setRecipientPhone(String recipientPhone) { this.recipientPhone = recipientPhone; }

    public ChannelEnum getChannel() { return channel; }
    public void setChannel(ChannelEnum channel) { this.channel = channel; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public NotificationStatusEnum getStatus() { return status; }
    public void setStatus(NotificationStatusEnum status) { this.status = status; }

    public Instant getSentAt() { return sentAt; }
    public void setSentAt(Instant sentAt) { this.sentAt = sentAt; }

    public Instant getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(Instant deliveredAt) { this.deliveredAt = deliveredAt; }

    public Instant getReadAt() { return readAt; }
    public void setReadAt(Instant readAt) { this.readAt = readAt; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }

    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }

    public String getExternalReference() { return externalReference; }
    public void setExternalReference(String externalReference) { this.externalReference = externalReference; }
}
