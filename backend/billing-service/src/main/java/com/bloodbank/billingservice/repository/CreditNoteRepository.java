package com.bloodbank.billingservice.repository;

import com.bloodbank.billingservice.entity.CreditNote;
import com.bloodbank.billingservice.enums.CreditNoteStatusEnum;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.QueryHint;

@Repository
public interface CreditNoteRepository extends JpaRepository<CreditNote, UUID>,
                                              JpaSpecificationExecutor<CreditNote> {

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<CreditNote> findByInvoiceId(UUID invoiceId);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    Optional<CreditNote> findByCreditNoteNumber(String creditNoteNumber);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<CreditNote> findByStatus(CreditNoteStatusEnum status);
}
