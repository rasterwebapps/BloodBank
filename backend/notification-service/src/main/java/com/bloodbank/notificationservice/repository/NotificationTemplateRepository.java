package com.bloodbank.notificationservice.repository;

import com.bloodbank.notificationservice.entity.NotificationTemplate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.QueryHint;

@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, UUID>,
                                                        JpaSpecificationExecutor<NotificationTemplate> {

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    Optional<NotificationTemplate> findByTemplateCode(String templateCode);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<NotificationTemplate> findByIsActiveTrue();

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<NotificationTemplate> findByLanguage(String language);
}
