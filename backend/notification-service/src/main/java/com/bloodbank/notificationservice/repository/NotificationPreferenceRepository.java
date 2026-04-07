package com.bloodbank.notificationservice.repository;

import com.bloodbank.notificationservice.entity.NotificationPreference;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, UUID>,
                                                          JpaSpecificationExecutor<NotificationPreference> {

    List<NotificationPreference> findByUserId(UUID userId);
}
