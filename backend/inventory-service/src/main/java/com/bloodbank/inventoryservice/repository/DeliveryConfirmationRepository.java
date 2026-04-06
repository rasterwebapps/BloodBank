package com.bloodbank.inventoryservice.repository;

import com.bloodbank.inventoryservice.entity.DeliveryConfirmation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface DeliveryConfirmationRepository extends JpaRepository<DeliveryConfirmation, UUID>,
                                                        JpaSpecificationExecutor<DeliveryConfirmation> {

    List<DeliveryConfirmation> findByTransportRequestId(UUID transportRequestId);
}
