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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreditNoteServiceTest {

    @Mock
    private CreditNoteRepository creditNoteRepository;

    @Mock
    private CreditNoteMapper creditNoteMapper;

    @Mock
    private InvoiceRepository invoiceRepository;

    @InjectMocks
    private CreditNoteService creditNoteService;

    private UUID creditNoteId;
    private UUID invoiceId;
    private UUID branchId;
    private CreditNote creditNote;
    private CreditNoteResponse creditNoteResponse;
    private CreditNoteCreateRequest createRequest;
    private Invoice invoice;

    @BeforeEach
    void setUp() {
        creditNoteId = UUID.randomUUID();
        invoiceId = UUID.randomUUID();
        branchId = UUID.randomUUID();

        creditNote = new CreditNote(invoiceId, new BigDecimal("50.00"), "Overcharge");
        creditNote.setId(creditNoteId);
        creditNote.setBranchId(branchId);
        creditNote.setCreditNoteNumber("CN-ABCD1234");
        creditNote.setStatus(CreditNoteStatusEnum.ISSUED);

        creditNoteResponse = new CreditNoteResponse(
                creditNoteId, invoiceId, "CN-ABCD1234", Instant.now(),
                new BigDecimal("50.00"), "Overcharge", CreditNoteStatusEnum.ISSUED,
                null, null,
                branchId, LocalDateTime.now(), LocalDateTime.now()
        );

        createRequest = new CreditNoteCreateRequest(
                invoiceId, new BigDecimal("50.00"), "Overcharge", null, branchId
        );

        invoice = new Invoice(UUID.randomUUID(), "INV-ABCD1234");
        invoice.setId(invoiceId);
        invoice.setBranchId(branchId);
        invoice.setStatus(InvoiceStatusEnum.ISSUED);
        invoice.setTotalAmount(new BigDecimal("200.00"));
        invoice.setAmountPaid(BigDecimal.ZERO);
        invoice.setBalanceDue(new BigDecimal("200.00"));
    }

    @Nested
    @DisplayName("createCreditNote")
    class CreateCreditNote {

        @Test
        @DisplayName("should create credit note successfully")
        void shouldCreateCreditNoteSuccessfully() {
            when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
            when(creditNoteMapper.toEntity(createRequest)).thenReturn(creditNote);
            when(creditNoteRepository.save(any(CreditNote.class))).thenReturn(creditNote);
            when(creditNoteMapper.toResponse(creditNote)).thenReturn(creditNoteResponse);

            CreditNoteResponse result = creditNoteService.createCreditNote(createRequest);

            assertThat(result).isNotNull();
            assertThat(result.reason()).isEqualTo("Overcharge");
            verify(creditNoteRepository).save(any(CreditNote.class));
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when invoice not found")
        void shouldThrowResourceNotFoundExceptionWhenInvoiceNotFound() {
            when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> creditNoteService.createCreditNote(createRequest))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getCreditNoteById")
    class GetCreditNoteById {

        @Test
        @DisplayName("should return credit note when found")
        void shouldReturnCreditNoteWhenFound() {
            when(creditNoteRepository.findById(creditNoteId)).thenReturn(Optional.of(creditNote));
            when(creditNoteMapper.toResponse(creditNote)).thenReturn(creditNoteResponse);

            CreditNoteResponse result = creditNoteService.getCreditNoteById(creditNoteId);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(creditNoteId);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when not found")
        void shouldThrowResourceNotFoundExceptionWhenNotFound() {
            when(creditNoteRepository.findById(creditNoteId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> creditNoteService.getCreditNoteById(creditNoteId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("applyCreditNote")
    class ApplyCreditNote {

        @Test
        @DisplayName("should apply credit note to target invoice")
        void shouldApplyCreditNoteToTargetInvoice() {
            UUID targetInvoiceId = UUID.randomUUID();
            Invoice targetInvoice = new Invoice(UUID.randomUUID(), "INV-TARGET");
            targetInvoice.setId(targetInvoiceId);
            targetInvoice.setStatus(InvoiceStatusEnum.ISSUED);
            targetInvoice.setTotalAmount(new BigDecimal("200.00"));
            targetInvoice.setAmountPaid(BigDecimal.ZERO);

            CreditNoteResponse appliedResponse = new CreditNoteResponse(
                    creditNoteId, invoiceId, "CN-ABCD1234", Instant.now(),
                    new BigDecimal("50.00"), "Overcharge", CreditNoteStatusEnum.APPLIED,
                    targetInvoiceId, null,
                    branchId, LocalDateTime.now(), LocalDateTime.now()
            );

            when(creditNoteRepository.findById(creditNoteId)).thenReturn(Optional.of(creditNote));
            when(invoiceRepository.findById(targetInvoiceId)).thenReturn(Optional.of(targetInvoice));
            when(invoiceRepository.save(any(Invoice.class))).thenReturn(targetInvoice);
            when(creditNoteRepository.save(any(CreditNote.class))).thenReturn(creditNote);
            when(creditNoteMapper.toResponse(creditNote)).thenReturn(appliedResponse);

            CreditNoteResponse result = creditNoteService.applyCreditNote(creditNoteId, targetInvoiceId);

            assertThat(result).isNotNull();
            assertThat(creditNote.getStatus()).isEqualTo(CreditNoteStatusEnum.APPLIED);
            assertThat(creditNote.getAppliedToInvoice()).isEqualTo(targetInvoiceId);
            verify(invoiceRepository).save(targetInvoice);
        }

        @Test
        @DisplayName("should throw BusinessException when credit note is not ISSUED")
        void shouldThrowBusinessExceptionWhenCreditNoteIsNotIssued() {
            creditNote.setStatus(CreditNoteStatusEnum.APPLIED);
            when(creditNoteRepository.findById(creditNoteId)).thenReturn(Optional.of(creditNote));

            assertThatThrownBy(() -> creditNoteService.applyCreditNote(creditNoteId, UUID.randomUUID()))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("voidCreditNote")
    class VoidCreditNote {

        @Test
        @DisplayName("should void credit note successfully")
        void shouldVoidCreditNoteSuccessfully() {
            CreditNoteResponse voidedResponse = new CreditNoteResponse(
                    creditNoteId, invoiceId, "CN-ABCD1234", Instant.now(),
                    new BigDecimal("50.00"), "Overcharge", CreditNoteStatusEnum.VOID,
                    null, null,
                    branchId, LocalDateTime.now(), LocalDateTime.now()
            );

            when(creditNoteRepository.findById(creditNoteId)).thenReturn(Optional.of(creditNote));
            when(creditNoteRepository.save(any(CreditNote.class))).thenReturn(creditNote);
            when(creditNoteMapper.toResponse(creditNote)).thenReturn(voidedResponse);

            CreditNoteResponse result = creditNoteService.voidCreditNote(creditNoteId);

            assertThat(result).isNotNull();
            assertThat(creditNote.getStatus()).isEqualTo(CreditNoteStatusEnum.VOID);
        }

        @Test
        @DisplayName("should throw BusinessException when credit note is applied")
        void shouldThrowBusinessExceptionWhenCreditNoteIsApplied() {
            creditNote.setStatus(CreditNoteStatusEnum.APPLIED);
            when(creditNoteRepository.findById(creditNoteId)).thenReturn(Optional.of(creditNote));

            assertThatThrownBy(() -> creditNoteService.voidCreditNote(creditNoteId))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("getCreditNotesByInvoice")
    class GetCreditNotesByInvoice {

        @Test
        @DisplayName("should return credit notes for invoice")
        void shouldReturnCreditNotesForInvoice() {
            List<CreditNote> creditNotes = List.of(creditNote);
            List<CreditNoteResponse> responses = List.of(creditNoteResponse);
            when(creditNoteRepository.findByInvoiceId(invoiceId)).thenReturn(creditNotes);
            when(creditNoteMapper.toResponseList(creditNotes)).thenReturn(responses);

            List<CreditNoteResponse> result = creditNoteService.getCreditNotesByInvoice(invoiceId);

            assertThat(result).hasSize(1);
            verify(creditNoteRepository).findByInvoiceId(invoiceId);
        }
    }
}
