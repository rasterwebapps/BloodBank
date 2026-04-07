package com.bloodbank.notificationservice.service;

import com.bloodbank.common.exceptions.ResourceNotFoundException;
import com.bloodbank.notificationservice.dto.NotificationTemplateCreateRequest;
import com.bloodbank.notificationservice.dto.NotificationTemplateResponse;
import com.bloodbank.notificationservice.entity.NotificationTemplate;
import com.bloodbank.notificationservice.mapper.NotificationTemplateMapper;
import com.bloodbank.notificationservice.repository.NotificationTemplateRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class TemplateService {

    private static final Logger log = LoggerFactory.getLogger(TemplateService.class);

    private final NotificationTemplateRepository templateRepository;
    private final NotificationTemplateMapper templateMapper;

    public TemplateService(NotificationTemplateRepository templateRepository,
                           NotificationTemplateMapper templateMapper) {
        this.templateRepository = templateRepository;
        this.templateMapper = templateMapper;
    }

    @Transactional
    public NotificationTemplateResponse create(NotificationTemplateCreateRequest request) {
        log.info("Creating notification template: code={}, name={}",
                request.templateCode(), request.templateName());
        NotificationTemplate template = templateMapper.toEntity(request);
        template.setActive(true);
        template = templateRepository.save(template);
        return templateMapper.toResponse(template);
    }

    public NotificationTemplateResponse getById(UUID id) {
        NotificationTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("NotificationTemplate", "id", id));
        return templateMapper.toResponse(template);
    }

    public List<NotificationTemplateResponse> getAll() {
        return templateMapper.toResponseList(templateRepository.findAll());
    }

    public List<NotificationTemplateResponse> getActiveTemplates() {
        return templateMapper.toResponseList(templateRepository.findByIsActiveTrue());
    }

    public List<NotificationTemplateResponse> getByLanguage(String language) {
        return templateMapper.toResponseList(templateRepository.findByLanguage(language));
    }

    @Transactional
    public NotificationTemplateResponse deactivate(UUID id) {
        log.info("Deactivating notification template: id={}", id);
        NotificationTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("NotificationTemplate", "id", id));
        template.setActive(false);
        template = templateRepository.save(template);
        return templateMapper.toResponse(template);
    }
}
