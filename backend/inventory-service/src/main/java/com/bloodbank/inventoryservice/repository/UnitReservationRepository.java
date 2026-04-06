package com.bloodbank.inventoryservice.repository;

import com.bloodbank.inventoryservice.entity.UnitReservation;
import com.bloodbank.inventoryservice.enums.ReservationStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface UnitReservationRepository extends JpaRepository<UnitReservation, UUID>,
                                                   JpaSpecificationExecutor<UnitReservation> {

    List<UnitReservation> findByComponentId(UUID componentId);

    List<UnitReservation> findByStatus(ReservationStatusEnum status);

    List<UnitReservation> findByExpiryDateBeforeAndStatus(Instant expiryDate, ReservationStatusEnum status);
}
