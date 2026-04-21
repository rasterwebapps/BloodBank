package com.bloodbank.inventoryservice.repository;

import com.bloodbank.inventoryservice.entity.DeliveryConfirmation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.QueryHint;

public interface DeliveryConfirmationRepository extends JpaRepository<DeliveryConfirmation, UUID>,
                                                        JpaSpecificationExecutor<DeliveryConfirmation> {

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<DeliveryConfirmation> findByTransportRequestId(UUID transportRequestId);
}
