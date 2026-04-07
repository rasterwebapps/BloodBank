package com.bloodbank.billingservice.service;

import com.bloodbank.common.exceptions.BusinessException;
import com.bloodbank.common.exceptions.ResourceNotFoundException;
import com.bloodbank.billingservice.dto.PaymentCreateRequest;
import com.bloodbank.billingservice.dto.PaymentResponse;
import com.bloodbank.billingservice.entity.Invoice;
import com.bloodbank.billingservice.entity.Payment;
import com.bloodbank.billingservice.enums.InvoiceStatusEnum;
import com.bloodbank.billingservice.enums.PaymentStatusEnum;
import com.bloodbank.billingservice.mapper.PaymentMapper;
import com.bloodbank.billingservice.repository.InvoiceRepository;
import com.bloodbank.billingservice.repository.PaymentRepository;

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
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final InvoiceRepository invoiceRepository;

    public PaymentService(PaymentRepository paymentRepository,
                          PaymentMapper paymentMapper,
                          InvoiceRepository invoiceRepository) {
        this.paymentRepository = paymentRepository;
        this.paymentMapper = paymentMapper;
        this.invoiceRepository = invoiceRepository;
    }

    @Transactional
    public PaymentResponse recordPayment(PaymentCreateRequest request) {
        log.info("Recording payment for invoiceId={}, amount={}", request.invoiceId(), request.amount());

        Invoice invoice = invoiceRepository.findById(request.invoiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", request.invoiceId()));

        if (invoice.getStatus() == InvoiceStatusEnum.VOID || invoice.getStatus() == InvoiceStatusEnum.CANCELLED) {
            throw new BusinessException("Cannot record payment on a voided or cancelled invoice", "INVOICE_CLOSED");
        }

        Payment payment = paymentMapper.toEntity(request);
        payment.setBranchId(request.branchId());
        payment.setPaymentNumber("PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        payment.setPaymentDate(Instant.now());
        payment.setStatus(PaymentStatusEnum.COMPLETED);
        if (request.currency() != null) {
            payment.setCurrency(request.currency());
        }
        payment = paymentRepository.save(payment);

        BigDecimal newAmountPaid = (invoice.getAmountPaid() != null ? invoice.getAmountPaid() : BigDecimal.ZERO)
                .add(request.amount());
        invoice.setAmountPaid(newAmountPaid);

        BigDecimal totalAmount = invoice.getTotalAmount() != null ? invoice.getTotalAmount() : BigDecimal.ZERO;
        BigDecimal newBalance = totalAmount.subtract(newAmountPaid);
        invoice.setBalanceDue(newBalance.max(BigDecimal.ZERO));

        if (newAmountPaid.compareTo(totalAmount) >= 0) {
            invoice.setStatus(InvoiceStatusEnum.PAID);
        } else if (newAmountPaid.compareTo(BigDecimal.ZERO) > 0) {
            invoice.setStatus(InvoiceStatusEnum.PARTIALLY_PAID);
        }

        invoiceRepository.save(invoice);
        log.info("Payment recorded: paymentNumber={}, invoiceStatus={}", payment.getPaymentNumber(), invoice.getStatus());
        return paymentMapper.toResponse(payment);
    }

    public PaymentResponse getPaymentById(UUID id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", id));
        return paymentMapper.toResponse(payment);
    }

    public List<PaymentResponse> getPaymentsByInvoice(UUID invoiceId) {
        return paymentMapper.toResponseList(paymentRepository.findByInvoiceId(invoiceId));
    }
}
