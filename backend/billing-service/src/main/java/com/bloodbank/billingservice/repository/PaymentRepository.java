package com.bloodbank.billingservice.repository;

import com.bloodbank.billingservice.entity.Payment;
import com.bloodbank.billingservice.enums.PaymentStatusEnum;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID>,
                                           JpaSpecificationExecutor<Payment> {

    List<Payment> findByInvoiceId(UUID invoiceId);

    Optional<Payment> findByPaymentNumber(String paymentNumber);

    List<Payment> findByStatus(PaymentStatusEnum status);
}
