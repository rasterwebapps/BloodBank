package com.bloodbank.notificationservice.repository;

import com.bloodbank.notificationservice.entity.NotificationTemplate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, UUID>,
                                                        JpaSpecificationExecutor<NotificationTemplate> {

    Optional<NotificationTemplate> findByTemplateCode(String templateCode);

    List<NotificationTemplate> findByIsActiveTrue();

    List<NotificationTemplate> findByLanguage(String language);
}
