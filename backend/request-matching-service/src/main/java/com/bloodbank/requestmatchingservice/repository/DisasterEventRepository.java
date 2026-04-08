package com.bloodbank.requestmatchingservice.repository;

import com.bloodbank.requestmatchingservice.entity.DisasterEvent;
import com.bloodbank.requestmatchingservice.enums.DisasterStatusEnum;
import com.bloodbank.requestmatchingservice.enums.DisasterTypeEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DisasterEventRepository extends JpaRepository<DisasterEvent, UUID>,
                                                  JpaSpecificationExecutor<DisasterEvent> {
    Optional<DisasterEvent> findByEventCode(String eventCode);
    Page<DisasterEvent> findByStatus(DisasterStatusEnum status, Pageable pageable);
    List<DisasterEvent> findByStatusIn(List<DisasterStatusEnum> statuses);
    List<DisasterEvent> findByEventType(DisasterTypeEnum eventType);
    boolean existsByEventCode(String eventCode);
}
