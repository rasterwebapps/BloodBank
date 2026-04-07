package com.bloodbank.billingservice.entity;

import com.bloodbank.billingservice.enums.CreditNoteStatusEnum;
import com.bloodbank.common.model.BranchScopedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "credit_notes")
@FilterDef(name = "branchFilter", parameters = @ParamDef(name = "branchId", type = UUID.class))
@Filter(name = "branchFilter", condition = "branch_id = :branchId")
public class CreditNote extends BranchScopedEntity {

    @Column(name = "invoice_id", nullable = false)
    private UUID invoiceId;

    @Column(name = "credit_note_number", unique = true, length = 30)
    private String creditNoteNumber;

    @Column(name = "credit_date")
    private Instant creditDate;

    @Column(name = "amount", precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "reason", length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private CreditNoteStatusEnum status = CreditNoteStatusEnum.ISSUED;

    @Column(name = "applied_to_invoice")
    private UUID appliedToInvoice;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    protected CreditNote() {}

    public CreditNote(UUID invoiceId, BigDecimal amount, String reason) {
        this.invoiceId = invoiceId;
        this.amount = amount;
        this.reason = reason;
        this.creditDate = Instant.now();
    }

    public UUID getInvoiceId() { return invoiceId; }
    public void setInvoiceId(UUID invoiceId) { this.invoiceId = invoiceId; }

    public String getCreditNoteNumber() { return creditNoteNumber; }
    public void setCreditNoteNumber(String creditNoteNumber) { this.creditNoteNumber = creditNoteNumber; }

    public Instant getCreditDate() { return creditDate; }
    public void setCreditDate(Instant creditDate) { this.creditDate = creditDate; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public CreditNoteStatusEnum getStatus() { return status; }
    public void setStatus(CreditNoteStatusEnum status) { this.status = status; }

    public UUID getAppliedToInvoice() { return appliedToInvoice; }
    public void setAppliedToInvoice(UUID appliedToInvoice) { this.appliedToInvoice = appliedToInvoice; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
