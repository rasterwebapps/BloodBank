package com.bloodbank.billingservice.repository;

import com.bloodbank.billingservice.entity.CreditNote;
import com.bloodbank.billingservice.enums.CreditNoteStatusEnum;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CreditNoteRepository extends JpaRepository<CreditNote, UUID>,
                                              JpaSpecificationExecutor<CreditNote> {

    List<CreditNote> findByInvoiceId(UUID invoiceId);

    Optional<CreditNote> findByCreditNoteNumber(String creditNoteNumber);

    List<CreditNote> findByStatus(CreditNoteStatusEnum status);
}
