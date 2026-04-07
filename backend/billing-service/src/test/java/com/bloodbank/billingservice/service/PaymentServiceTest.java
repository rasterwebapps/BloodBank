package com.bloodbank.billingservice.service;

import com.bloodbank.common.exceptions.BusinessException;
import com.bloodbank.common.exceptions.ResourceNotFoundException;
import com.bloodbank.billingservice.dto.PaymentCreateRequest;
import com.bloodbank.billingservice.dto.PaymentResponse;
import com.bloodbank.billingservice.entity.Invoice;
import com.bloodbank.billingservice.entity.Payment;
import com.bloodbank.billingservice.enums.InvoiceStatusEnum;
import com.bloodbank.billingservice.enums.PaymentMethodEnum;
import com.bloodbank.billingservice.enums.PaymentStatusEnum;
import com.bloodbank.billingservice.mapper.PaymentMapper;
import com.bloodbank.billingservice.repository.InvoiceRepository;
import com.bloodbank.billingservice.repository.PaymentRepository;

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
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private InvoiceRepository invoiceRepository;

    @InjectMocks
    private PaymentService paymentService;

    private UUID paymentId;
    private UUID invoiceId;
    private UUID branchId;
    private Invoice invoice;
    private Payment payment;
    private PaymentResponse paymentResponse;
    private PaymentCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        paymentId = UUID.randomUUID();
        invoiceId = UUID.randomUUID();
        branchId = UUID.randomUUID();

        invoice = new Invoice(UUID.randomUUID(), "INV-ABCD1234");
        invoice.setId(invoiceId);
        invoice.setBranchId(branchId);
        invoice.setStatus(InvoiceStatusEnum.ISSUED);
        invoice.setTotalAmount(new BigDecimal("200.00"));
        invoice.setAmountPaid(BigDecimal.ZERO);
        invoice.setBalanceDue(new BigDecimal("200.00"));

        payment = new Payment(invoiceId, new BigDecimal("100.00"), PaymentMethodEnum.CASH);
        payment.setId(paymentId);
        payment.setBranchId(branchId);
        payment.setPaymentNumber("PAY-ABCD1234");

        paymentResponse = new PaymentResponse(
                paymentId, invoiceId, "PAY-ABCD1234", Instant.now(),
                new BigDecimal("100.00"), "USD", PaymentMethodEnum.CASH,
                null, PaymentStatusEnum.COMPLETED, null,
                branchId, LocalDateTime.now(), LocalDateTime.now()
        );

        createRequest = new PaymentCreateRequest(
                invoiceId, new BigDecimal("100.00"), "USD",
                PaymentMethodEnum.CASH, null, null, branchId
        );
    }

    @Nested
    @DisplayName("recordPayment")
    class RecordPayment {

        @Test
        @DisplayName("should record payment and update invoice — partial payment")
        void shouldRecordPaymentAndUpdateInvoicePartialPayment() {
            when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
            when(paymentMapper.toEntity(createRequest)).thenReturn(payment);
            when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
            when(paymentMapper.toResponse(payment)).thenReturn(paymentResponse);
            when(invoiceRepository.save(any(Invoice.class))).thenReturn(invoice);

            PaymentResponse result = paymentService.recordPayment(createRequest);

            assertThat(result).isNotNull();
            assertThat(result.amount()).isEqualTo(new BigDecimal("100.00"));
            assertThat(invoice.getAmountPaid()).isEqualByComparingTo(new BigDecimal("100.00"));
            assertThat(invoice.getStatus()).isEqualTo(InvoiceStatusEnum.PARTIALLY_PAID);
            verify(invoiceRepository).save(invoice);
        }

        @Test
        @DisplayName("should mark invoice as PAID when fully paid")
        void shouldMarkInvoiceAsPaidWhenFullyPaid() {
            PaymentCreateRequest fullPayment = new PaymentCreateRequest(
                    invoiceId, new BigDecimal("200.00"), "USD",
                    PaymentMethodEnum.BANK_TRANSFER, null, null, branchId
            );
            Payment fullPay = new Payment(invoiceId, new BigDecimal("200.00"), PaymentMethodEnum.BANK_TRANSFER);
            fullPay.setId(paymentId);

            when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
            when(paymentMapper.toEntity(fullPayment)).thenReturn(fullPay);
            when(paymentRepository.save(any(Payment.class))).thenReturn(fullPay);
            when(paymentMapper.toResponse(fullPay)).thenReturn(paymentResponse);
            when(invoiceRepository.save(any(Invoice.class))).thenReturn(invoice);

            paymentService.recordPayment(fullPayment);

            assertThat(invoice.getStatus()).isEqualTo(InvoiceStatusEnum.PAID);
        }

        @Test
        @DisplayName("should throw BusinessException when invoice is voided")
        void shouldThrowBusinessExceptionWhenInvoiceIsVoided() {
            invoice.setStatus(InvoiceStatusEnum.VOID);
            when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));

            assertThatThrownBy(() -> paymentService.recordPayment(createRequest))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when invoice not found")
        void shouldThrowResourceNotFoundExceptionWhenInvoiceNotFound() {
            when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> paymentService.recordPayment(createRequest))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getPaymentById")
    class GetPaymentById {

        @Test
        @DisplayName("should return payment when found")
        void shouldReturnPaymentWhenFound() {
            when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
            when(paymentMapper.toResponse(payment)).thenReturn(paymentResponse);

            PaymentResponse result = paymentService.getPaymentById(paymentId);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(paymentId);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when not found")
        void shouldThrowResourceNotFoundExceptionWhenNotFound() {
            when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> paymentService.getPaymentById(paymentId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getPaymentsByInvoice")
    class GetPaymentsByInvoice {

        @Test
        @DisplayName("should return payments for invoice")
        void shouldReturnPaymentsForInvoice() {
            List<Payment> payments = List.of(payment);
            List<PaymentResponse> responses = List.of(paymentResponse);
            when(paymentRepository.findByInvoiceId(invoiceId)).thenReturn(payments);
            when(paymentMapper.toResponseList(payments)).thenReturn(responses);

            List<PaymentResponse> result = paymentService.getPaymentsByInvoice(invoiceId);

            assertThat(result).hasSize(1);
            verify(paymentRepository).findByInvoiceId(invoiceId);
        }
    }
}
