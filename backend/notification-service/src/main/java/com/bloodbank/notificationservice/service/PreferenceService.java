package com.bloodbank.notificationservice.service;

import com.bloodbank.common.exceptions.ResourceNotFoundException;
import com.bloodbank.notificationservice.dto.NotificationPreferenceCreateRequest;
import com.bloodbank.notificationservice.dto.NotificationPreferenceResponse;
import com.bloodbank.notificationservice.entity.NotificationPreference;
import com.bloodbank.notificationservice.mapper.NotificationPreferenceMapper;
import com.bloodbank.notificationservice.repository.NotificationPreferenceRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class PreferenceService {

    private static final Logger log = LoggerFactory.getLogger(PreferenceService.class);

    private final NotificationPreferenceRepository preferenceRepository;
    private final NotificationPreferenceMapper preferenceMapper;

    public PreferenceService(NotificationPreferenceRepository preferenceRepository,
                             NotificationPreferenceMapper preferenceMapper) {
        this.preferenceRepository = preferenceRepository;
        this.preferenceMapper = preferenceMapper;
    }

    @Transactional
    public NotificationPreferenceResponse create(NotificationPreferenceCreateRequest request) {
        log.info("Creating notification preference: userId={}, channel={}, eventType={}",
                request.userId(), request.channel(), request.eventType());
        NotificationPreference preference = preferenceMapper.toEntity(request);
        preference = preferenceRepository.save(preference);
        return preferenceMapper.toResponse(preference);
    }

    public NotificationPreferenceResponse getById(UUID id) {
        NotificationPreference preference = preferenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("NotificationPreference", "id", id));
        return preferenceMapper.toResponse(preference);
    }

    public List<NotificationPreferenceResponse> getByUserId(UUID userId) {
        return preferenceMapper.toResponseList(preferenceRepository.findByUserId(userId));
    }

    @Transactional
    public NotificationPreferenceResponse togglePreference(UUID id) {
        log.info("Toggling notification preference: id={}", id);
        NotificationPreference preference = preferenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("NotificationPreference", "id", id));
        preference.setEnabled(!preference.isEnabled());
        preference = preferenceRepository.save(preference);
        return preferenceMapper.toResponse(preference);
    }
}
