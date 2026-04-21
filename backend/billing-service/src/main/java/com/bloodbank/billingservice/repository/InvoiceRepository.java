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
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.QueryHint;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, UUID>,
                                           JpaSpecificationExecutor<Invoice> {

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<Invoice> findByHospitalId(UUID hospitalId);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<Invoice> findByStatus(InvoiceStatusEnum status);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    Page<Invoice> findByBranchId(UUID branchId, Pageable pageable);
}
