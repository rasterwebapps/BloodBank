package com.bloodbank.billingservice.service;

import com.bloodbank.common.events.BloodRequestMatchedEvent;
import com.bloodbank.common.events.InvoiceGeneratedEvent;
import com.bloodbank.common.exceptions.BusinessException;
import com.bloodbank.common.exceptions.ResourceNotFoundException;
import com.bloodbank.billingservice.dto.InvoiceCreateRequest;
import com.bloodbank.billingservice.dto.InvoiceResponse;
import com.bloodbank.billingservice.dto.LineItemCreateRequest;
import com.bloodbank.billingservice.dto.LineItemResponse;
import com.bloodbank.billingservice.entity.Invoice;
import com.bloodbank.billingservice.entity.InvoiceLineItem;
import com.bloodbank.billingservice.enums.InvoiceStatusEnum;
import com.bloodbank.billingservice.event.EventPublisher;
import com.bloodbank.billingservice.mapper.InvoiceMapper;
import com.bloodbank.billingservice.mapper.LineItemMapper;
import com.bloodbank.billingservice.repository.InvoiceRepository;
import com.bloodbank.billingservice.repository.LineItemRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class InvoiceService {

    private static final Logger log = LoggerFactory.getLogger(InvoiceService.class);

    private final InvoiceRepository invoiceRepository;
    private final InvoiceMapper invoiceMapper;
    private final LineItemRepository lineItemRepository;
    private final LineItemMapper lineItemMapper;
    private final EventPublisher eventPublisher;

    public InvoiceService(InvoiceRepository invoiceRepository,
                          InvoiceMapper invoiceMapper,
                          LineItemRepository lineItemRepository,
                          LineItemMapper lineItemMapper,
                          EventPublisher eventPublisher) {
        this.invoiceRepository = invoiceRepository;
        this.invoiceMapper = invoiceMapper;
        this.lineItemRepository = lineItemRepository;
        this.lineItemMapper = lineItemMapper;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public InvoiceResponse createInvoice(InvoiceCreateRequest request) {
        log.info("Creating invoice for hospitalId={}", request.hospitalId());
        Invoice invoice = invoiceMapper.toEntity(request);
        invoice.setBranchId(request.branchId());
        invoice.setInvoiceNumber("INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        invoice.setInvoiceDate(Instant.now());
        invoice.setSubtotal(BigDecimal.ZERO);
        invoice.setTaxAmount(BigDecimal.ZERO);
        invoice.setDiscountAmount(BigDecimal.ZERO);
        invoice.setTotalAmount(BigDecimal.ZERO);
        invoice.setAmountPaid(BigDecimal.ZERO);
        invoice.setBalanceDue(BigDecimal.ZERO);
        invoice.setStatus(InvoiceStatusEnum.DRAFT);
        if (request.currency() != null) {
            invoice.setCurrency(request.currency());
        }
        invoice = invoiceRepository.save(invoice);
        return invoiceMapper.toResponse(invoice);
    }

    public InvoiceResponse getInvoiceById(UUID id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", id));
        return invoiceMapper.toResponse(invoice);
    }

    public List<InvoiceResponse> getInvoicesByHospital(UUID hospitalId) {
        return invoiceMapper.toResponseList(invoiceRepository.findByHospitalId(hospitalId));
    }

    public List<InvoiceResponse> getInvoicesByStatus(InvoiceStatusEnum status) {
        return invoiceMapper.toResponseList(invoiceRepository.findByStatus(status));
    }

    @Transactional
    public InvoiceResponse updateInvoiceStatus(UUID id, InvoiceStatusEnum status) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", id));

        if (invoice.getStatus() == InvoiceStatusEnum.VOID) {
            throw new BusinessException("Cannot update status of a voided invoice", "INVOICE_VOID");
        }

        invoice.setStatus(status);
        invoice = invoiceRepository.save(invoice);
        log.info("Updated invoice {} status to {}", id, status);

        if (status == InvoiceStatusEnum.ISSUED) {
            eventPublisher.publishInvoiceGenerated(new InvoiceGeneratedEvent(
                    invoice.getId(), invoice.getHospitalId(), invoice.getBranchId(), Instant.now()
            ));
        }

        return invoiceMapper.toResponse(invoice);
    }

    @Transactional
    public LineItemResponse addLineItem(LineItemCreateRequest request) {
        Invoice invoice = invoiceRepository.findById(request.invoiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", request.invoiceId()));

        InvoiceLineItem lineItem = lineItemMapper.toEntity(request);
        lineItem.setBranchId(request.branchId());

        BigDecimal subtotal = request.unitPrice().multiply(BigDecimal.valueOf(request.quantity()));
        BigDecimal taxPct = request.taxPercentage() != null ? request.taxPercentage() : BigDecimal.ZERO;
        BigDecimal taxAmt = subtotal.multiply(taxPct).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal discount = request.discountAmount() != null ? request.discountAmount() : BigDecimal.ZERO;
        BigDecimal lineTotal = subtotal.add(taxAmt).subtract(discount);

        lineItem.setTaxAmount(taxAmt);
        lineItem.setLineTotal(lineTotal);

        lineItem = lineItemRepository.save(lineItem);

        recalculateInvoiceTotals(invoice);

        return lineItemMapper.toResponse(lineItem);
    }

    @Transactional
    public InvoiceResponse recalculateInvoiceTotals(UUID invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", invoiceId));
        recalculateInvoiceTotals(invoice);
        return invoiceMapper.toResponse(invoice);
    }

    private void recalculateInvoiceTotals(Invoice invoice) {
        List<InvoiceLineItem> items = lineItemRepository.findByInvoiceId(invoice.getId());

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;

        for (InvoiceLineItem item : items) {
            BigDecimal itemSubtotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            subtotal = subtotal.add(itemSubtotal);
            if (item.getTaxAmount() != null) {
                totalTax = totalTax.add(item.getTaxAmount());
            }
            if (item.getDiscountAmount() != null) {
                totalDiscount = totalDiscount.add(item.getDiscountAmount());
            }
        }

        BigDecimal totalAmount = subtotal.add(totalTax).subtract(totalDiscount);
        BigDecimal balanceDue = totalAmount.subtract(
                invoice.getAmountPaid() != null ? invoice.getAmountPaid() : BigDecimal.ZERO);

        invoice.setSubtotal(subtotal);
        invoice.setTaxAmount(totalTax);
        invoice.setDiscountAmount(totalDiscount);
        invoice.setTotalAmount(totalAmount);
        invoice.setBalanceDue(balanceDue);

        invoiceRepository.save(invoice);
        log.info("Recalculated invoice {} totals: total={}, balance={}", invoice.getId(), totalAmount, balanceDue);
    }

    @Transactional
    public void createInvoiceFromMatchedRequest(BloodRequestMatchedEvent event) {
        log.info("Creating invoice from BloodRequestMatchedEvent: requestId={}, branchId={}",
                event.requestId(), event.branchId());

        Invoice invoice = new Invoice(event.requestId(), 
                "INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        invoice.setBranchId(event.branchId());
        invoice.setDueDate(LocalDate.now().plusDays(30));

        invoice = invoiceRepository.save(invoice);

        for (UUID unitId : event.matchedUnitIds()) {
            InvoiceLineItem lineItem = new InvoiceLineItem(
                    invoice.getId(), "Blood unit: " + unitId, 1, BigDecimal.ZERO);
            lineItem.setBranchId(event.branchId());
            lineItem.setBloodIssueId(unitId);
            lineItem.setTaxPercentage(BigDecimal.ZERO);
            lineItem.setTaxAmount(BigDecimal.ZERO);
            lineItem.setDiscountAmount(BigDecimal.ZERO);
            lineItem.setLineTotal(BigDecimal.ZERO);
            lineItemRepository.save(lineItem);
        }

        log.info("Invoice created from matched request: invoiceNumber={}", invoice.getInvoiceNumber());
    }
}
