package com.bloodbank.notificationservice.entity;

import com.bloodbank.common.model.BaseEntity;
import com.bloodbank.notificationservice.enums.ChannelEnum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "notification_templates")
public class NotificationTemplate extends BaseEntity {

    @Column(name = "template_code", nullable = false, unique = true, length = 50)
    private String templateCode;

    @Column(name = "template_name", nullable = false, length = 200)
    private String templateName;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 20)
    private ChannelEnum channel;

    @Column(name = "subject", length = 500)
    private String subject;

    @Column(name = "body_template", columnDefinition = "TEXT")
    private String bodyTemplate;

    @Column(name = "variables", columnDefinition = "TEXT")
    private String variables;

    @Column(name = "language", length = 10)
    private String language = "en";

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    protected NotificationTemplate() {}

    public NotificationTemplate(String templateCode, String templateName, ChannelEnum channel,
                                String subject, String bodyTemplate) {
        this.templateCode = templateCode;
        this.templateName = templateName;
        this.channel = channel;
        this.subject = subject;
        this.bodyTemplate = bodyTemplate;
        this.active = true;
    }

    public String getTemplateCode() { return templateCode; }
    public void setTemplateCode(String templateCode) { this.templateCode = templateCode; }

    public String getTemplateName() { return templateName; }
    public void setTemplateName(String templateName) { this.templateName = templateName; }

    public ChannelEnum getChannel() { return channel; }
    public void setChannel(ChannelEnum channel) { this.channel = channel; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getBodyTemplate() { return bodyTemplate; }
    public void setBodyTemplate(String bodyTemplate) { this.bodyTemplate = bodyTemplate; }

    public String getVariables() { return variables; }
    public void setVariables(String variables) { this.variables = variables; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
