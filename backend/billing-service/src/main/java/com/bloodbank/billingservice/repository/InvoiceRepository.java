package com.bloodbank.billingservice.repository;

import com.bloodbank.billingservice.entity.Invoice;
import com.bloodbank.billingservice.enums.InvoiceStatusEnum;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, UUID>,
                                           JpaSpecificationExecutor<Invoice> {

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    List<Invoice> findByHospitalId(UUID hospitalId);

    List<Invoice> findByStatus(InvoiceStatusEnum status);

    Page<Invoice> findByBranchId(UUID branchId, Pageable pageable);
}
