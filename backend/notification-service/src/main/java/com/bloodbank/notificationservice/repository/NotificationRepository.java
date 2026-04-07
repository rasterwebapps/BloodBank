package com.bloodbank.notificationservice.repository;

import com.bloodbank.notificationservice.entity.Notification;
import com.bloodbank.notificationservice.enums.NotificationStatusEnum;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID>,
                                                JpaSpecificationExecutor<Notification> {

    List<Notification> findByRecipientId(UUID recipientId);

    List<Notification> findByStatus(NotificationStatusEnum status);

    List<Notification> findByBranchId(UUID branchId);
}
