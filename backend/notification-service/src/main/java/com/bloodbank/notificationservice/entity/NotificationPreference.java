package com.bloodbank.notificationservice.entity;

import com.bloodbank.common.model.BaseEntity;
import com.bloodbank.notificationservice.enums.ChannelEnum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "notification_preferences")
public class NotificationPreference extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 20)
    private ChannelEnum channel;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(name = "is_enabled", nullable = false)
    private boolean enabled = true;

    @Column(name = "quiet_hours_start")
    private LocalTime quietHoursStart;

    @Column(name = "quiet_hours_end")
    private LocalTime quietHoursEnd;

    protected NotificationPreference() {}

    public NotificationPreference(UUID userId, ChannelEnum channel, String eventType) {
        this.userId = userId;
        this.channel = channel;
        this.eventType = eventType;
        this.enabled = true;
    }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public ChannelEnum getChannel() { return channel; }
    public void setChannel(ChannelEnum channel) { this.channel = channel; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public LocalTime getQuietHoursStart() { return quietHoursStart; }
    public void setQuietHoursStart(LocalTime quietHoursStart) { this.quietHoursStart = quietHoursStart; }

    public LocalTime getQuietHoursEnd() { return quietHoursEnd; }
    public void setQuietHoursEnd(LocalTime quietHoursEnd) { this.quietHoursEnd = quietHoursEnd; }
}
