package com.bloodbank.notificationservice.repository;

import com.bloodbank.notificationservice.entity.Notification;
import com.bloodbank.notificationservice.enums.NotificationStatusEnum;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.QueryHint;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID>,
                                                JpaSpecificationExecutor<Notification> {

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<Notification> findByRecipientId(UUID recipientId);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<Notification> findByStatus(NotificationStatusEnum status);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<Notification> findByBranchId(UUID branchId);
}
