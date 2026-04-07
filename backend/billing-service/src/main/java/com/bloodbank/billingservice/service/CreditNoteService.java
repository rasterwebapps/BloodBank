package com.bloodbank.billingservice.service;

import com.bloodbank.common.exceptions.BusinessException;
import com.bloodbank.common.exceptions.ResourceNotFoundException;
import com.bloodbank.billingservice.dto.CreditNoteCreateRequest;
import com.bloodbank.billingservice.dto.CreditNoteResponse;
import com.bloodbank.billingservice.entity.CreditNote;
import com.bloodbank.billingservice.entity.Invoice;
import com.bloodbank.billingservice.enums.CreditNoteStatusEnum;
import com.bloodbank.billingservice.enums.InvoiceStatusEnum;
import com.bloodbank.billingservice.mapper.CreditNoteMapper;
import com.bloodbank.billingservice.repository.CreditNoteRepository;
import com.bloodbank.billingservice.repository.InvoiceRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class CreditNoteService {

    private static final Logger log = LoggerFactory.getLogger(CreditNoteService.class);

    private final CreditNoteRepository creditNoteRepository;
    private final CreditNoteMapper creditNoteMapper;
    private final InvoiceRepository invoiceRepository;

    public CreditNoteService(CreditNoteRepository creditNoteRepository,
                             CreditNoteMapper creditNoteMapper,
                             InvoiceRepository invoiceRepository) {
        this.creditNoteRepository = creditNoteRepository;
        this.creditNoteMapper = creditNoteMapper;
        this.invoiceRepository = invoiceRepository;
    }

    @Transactional
    public CreditNoteResponse createCreditNote(CreditNoteCreateRequest request) {
        log.info("Creating credit note for invoiceId={}, amount={}", request.invoiceId(), request.amount());

        invoiceRepository.findById(request.invoiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", request.invoiceId()));

        CreditNote creditNote = creditNoteMapper.toEntity(request);
        creditNote.setBranchId(request.branchId());
        creditNote.setCreditNoteNumber("CN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        creditNote.setCreditDate(Instant.now());
        creditNote.setStatus(CreditNoteStatusEnum.ISSUED);
        creditNote = creditNoteRepository.save(creditNote);
        return creditNoteMapper.toResponse(creditNote);
    }

    public CreditNoteResponse getCreditNoteById(UUID id) {
        CreditNote creditNote = creditNoteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CreditNote", "id", id));
        return creditNoteMapper.toResponse(creditNote);
    }

    public List<CreditNoteResponse> getCreditNotesByInvoice(UUID invoiceId) {
        return creditNoteMapper.toResponseList(creditNoteRepository.findByInvoiceId(invoiceId));
    }

    @Transactional
    public CreditNoteResponse applyCreditNote(UUID creditNoteId, UUID targetInvoiceId) {
        CreditNote creditNote = creditNoteRepository.findById(creditNoteId)
                .orElseThrow(() -> new ResourceNotFoundException("CreditNote", "id", creditNoteId));

        if (creditNote.getStatus() != CreditNoteStatusEnum.ISSUED) {
            throw new BusinessException("Credit note is not in ISSUED status", "CREDIT_NOTE_NOT_APPLICABLE");
        }

        Invoice targetInvoice = invoiceRepository.findById(targetInvoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", targetInvoiceId));

        if (targetInvoice.getStatus() == InvoiceStatusEnum.VOID
                || targetInvoice.getStatus() == InvoiceStatusEnum.CANCELLED) {
            throw new BusinessException("Cannot apply credit note to a voided or cancelled invoice",
                    "INVOICE_CLOSED");
        }

        BigDecimal newAmountPaid = (targetInvoice.getAmountPaid() != null
                ? targetInvoice.getAmountPaid() : BigDecimal.ZERO).add(creditNote.getAmount());
        targetInvoice.setAmountPaid(newAmountPaid);

        BigDecimal totalAmount = targetInvoice.getTotalAmount() != null
                ? targetInvoice.getTotalAmount() : BigDecimal.ZERO;
        BigDecimal newBalance = totalAmount.subtract(newAmountPaid);
        targetInvoice.setBalanceDue(newBalance.max(BigDecimal.ZERO));

        if (newAmountPaid.compareTo(totalAmount) >= 0) {
            targetInvoice.setStatus(InvoiceStatusEnum.PAID);
        } else if (newAmountPaid.compareTo(BigDecimal.ZERO) > 0) {
            targetInvoice.setStatus(InvoiceStatusEnum.PARTIALLY_PAID);
        }

        invoiceRepository.save(targetInvoice);

        creditNote.setStatus(CreditNoteStatusEnum.APPLIED);
        creditNote.setAppliedToInvoice(targetInvoiceId);
        creditNote = creditNoteRepository.save(creditNote);

        log.info("Applied credit note {} to invoice {}", creditNoteId, targetInvoiceId);
        return creditNoteMapper.toResponse(creditNote);
    }

    @Transactional
    public CreditNoteResponse voidCreditNote(UUID id) {
        CreditNote creditNote = creditNoteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CreditNote", "id", id));

        if (creditNote.getStatus() == CreditNoteStatusEnum.APPLIED) {
            throw new BusinessException("Cannot void an applied credit note", "CREDIT_NOTE_APPLIED");
        }

        creditNote.setStatus(CreditNoteStatusEnum.VOID);
        creditNote = creditNoteRepository.save(creditNote);
        log.info("Voided credit note: id={}", id);
        return creditNoteMapper.toResponse(creditNote);
    }
}
