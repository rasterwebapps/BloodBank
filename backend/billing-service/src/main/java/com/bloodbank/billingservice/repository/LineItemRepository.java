package com.bloodbank.billingservice.repository;

import com.bloodbank.billingservice.entity.InvoiceLineItem;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.QueryHint;

@Repository
public interface LineItemRepository extends JpaRepository<InvoiceLineItem, UUID>,
                                            JpaSpecificationExecutor<InvoiceLineItem> {

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<InvoiceLineItem> findByInvoiceId(UUID invoiceId);
}
