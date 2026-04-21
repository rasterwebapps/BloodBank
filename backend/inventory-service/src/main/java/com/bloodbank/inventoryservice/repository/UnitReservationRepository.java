package com.bloodbank.inventoryservice.repository;

import com.bloodbank.inventoryservice.entity.UnitReservation;
import com.bloodbank.inventoryservice.enums.ReservationStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.QueryHint;

public interface UnitReservationRepository extends JpaRepository<UnitReservation, UUID>,
                                                   JpaSpecificationExecutor<UnitReservation> {

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<UnitReservation> findByComponentId(UUID componentId);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<UnitReservation> findByStatus(ReservationStatusEnum status);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<UnitReservation> findByExpiryDateBeforeAndStatus(Instant expiryDate, ReservationStatusEnum status);
}
