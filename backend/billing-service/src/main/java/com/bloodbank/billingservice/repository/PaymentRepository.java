package com.bloodbank.billingservice.repository;

import com.bloodbank.billingservice.entity.Payment;
import com.bloodbank.billingservice.enums.PaymentStatusEnum;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.QueryHint;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID>,
                                           JpaSpecificationExecutor<Payment> {

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<Payment> findByInvoiceId(UUID invoiceId);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    Optional<Payment> findByPaymentNumber(String paymentNumber);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<Payment> findByStatus(PaymentStatusEnum status);
}
