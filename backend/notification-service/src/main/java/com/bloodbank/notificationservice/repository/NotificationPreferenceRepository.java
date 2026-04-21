package com.bloodbank.notificationservice.repository;

import com.bloodbank.notificationservice.entity.NotificationPreference;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.QueryHint;

@Repository
public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, UUID>,
                                                          JpaSpecificationExecutor<NotificationPreference> {

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<NotificationPreference> findByUserId(UUID userId);
}
