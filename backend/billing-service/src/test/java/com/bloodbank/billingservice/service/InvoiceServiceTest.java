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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private InvoiceMapper invoiceMapper;

    @Mock
    private LineItemRepository lineItemRepository;

    @Mock
    private LineItemMapper lineItemMapper;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private InvoiceService invoiceService;

    private UUID invoiceId;
    private UUID hospitalId;
    private UUID branchId;
    private Invoice invoice;
    private InvoiceResponse invoiceResponse;
    private InvoiceCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        invoiceId = UUID.randomUUID();
        hospitalId = UUID.randomUUID();
        branchId = UUID.randomUUID();

        invoice = new Invoice(hospitalId, "INV-ABCD1234");
        invoice.setId(invoiceId);
        invoice.setBranchId(branchId);
        invoice.setStatus(InvoiceStatusEnum.DRAFT);

        invoiceResponse = new InvoiceResponse(
                invoiceId, hospitalId, "INV-ABCD1234", Instant.now(),
                LocalDate.now().plusDays(30), BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                "USD", InvoiceStatusEnum.DRAFT, null,
                branchId, LocalDateTime.now(), LocalDateTime.now()
        );

        createRequest = new InvoiceCreateRequest(
                hospitalId, LocalDate.now().plusDays(30), "USD", null, branchId
        );
    }

    @Nested
    @DisplayName("createInvoice")
    class CreateInvoice {

        @Test
        @DisplayName("should create invoice successfully")
        void shouldCreateInvoiceSuccessfully() {
            when(invoiceMapper.toEntity(createRequest)).thenReturn(invoice);
            when(invoiceRepository.save(any(Invoice.class))).thenReturn(invoice);
            when(invoiceMapper.toResponse(invoice)).thenReturn(invoiceResponse);

            InvoiceResponse result = invoiceService.createInvoice(createRequest);

            assertThat(result).isNotNull();
            assertThat(result.hospitalId()).isEqualTo(hospitalId);
            verify(invoiceRepository).save(any(Invoice.class));
        }

        @Test
        @DisplayName("should set DRAFT status and generate invoice number")
        void shouldSetDraftStatusAndGenerateInvoiceNumber() {
            Invoice capturedInvoice = new Invoice(hospitalId, null);
            when(invoiceMapper.toEntity(createRequest)).thenReturn(capturedInvoice);
            when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));
            when(invoiceMapper.toResponse(any(Invoice.class))).thenReturn(invoiceResponse);

            invoiceService.createInvoice(createRequest);

            assertThat(capturedInvoice.getStatus()).isEqualTo(InvoiceStatusEnum.DRAFT);
            assertThat(capturedInvoice.getInvoiceNumber()).startsWith("INV-");
            assertThat(capturedInvoice.getInvoiceDate()).isNotNull();
        }
    }

    @Nested
    @DisplayName("getInvoiceById")
    class GetInvoiceById {

        @Test
        @DisplayName("should return invoice when found")
        void shouldReturnInvoiceWhenFound() {
            when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
            when(invoiceMapper.toResponse(invoice)).thenReturn(invoiceResponse);

            InvoiceResponse result = invoiceService.getInvoiceById(invoiceId);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(invoiceId);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when not found")
        void shouldThrowResourceNotFoundExceptionWhenNotFound() {
            when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> invoiceService.getInvoiceById(invoiceId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("updateInvoiceStatus")
    class UpdateInvoiceStatus {

        @Test
        @DisplayName("should update invoice status successfully")
        void shouldUpdateInvoiceStatusSuccessfully() {
            when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
            when(invoiceRepository.save(any(Invoice.class))).thenReturn(invoice);
            when(invoiceMapper.toResponse(invoice)).thenReturn(invoiceResponse);

            InvoiceResponse result = invoiceService.updateInvoiceStatus(invoiceId, InvoiceStatusEnum.ISSUED);

            assertThat(result).isNotNull();
            verify(invoiceRepository).save(invoice);
        }

        @Test
        @DisplayName("should publish event when status changes to ISSUED")
        void shouldPublishEventWhenStatusChangesToIssued() {
            when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
            when(invoiceRepository.save(any(Invoice.class))).thenReturn(invoice);
            when(invoiceMapper.toResponse(invoice)).thenReturn(invoiceResponse);

            invoiceService.updateInvoiceStatus(invoiceId, InvoiceStatusEnum.ISSUED);

            verify(eventPublisher).publishInvoiceGenerated(any(InvoiceGeneratedEvent.class));
        }

        @Test
        @DisplayName("should throw BusinessException when invoice is voided")
        void shouldThrowBusinessExceptionWhenInvoiceIsVoided() {
            invoice.setStatus(InvoiceStatusEnum.VOID);
            when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));

            assertThatThrownBy(() -> invoiceService.updateInvoiceStatus(invoiceId, InvoiceStatusEnum.ISSUED))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("addLineItem")
    class AddLineItem {

        @Test
        @DisplayName("should add line item and recalculate totals")
        void shouldAddLineItemAndRecalculateTotals() {
            UUID lineItemId = UUID.randomUUID();
            LineItemCreateRequest lineRequest = new LineItemCreateRequest(
                    invoiceId, null, null, "Blood unit", 2,
                    new BigDecimal("100.00"), new BigDecimal("5.00"), BigDecimal.ZERO, branchId
            );

            InvoiceLineItem lineItem = new InvoiceLineItem(invoiceId, "Blood unit", 2, new BigDecimal("100.00"));
            lineItem.setId(lineItemId);

            LineItemResponse lineItemResponse = new LineItemResponse(
                    lineItemId, invoiceId, null, null, "Blood unit", 2,
                    new BigDecimal("100.00"), new BigDecimal("5.00"), new BigDecimal("10.00"),
                    BigDecimal.ZERO, new BigDecimal("210.00"),
                    branchId, LocalDateTime.now(), LocalDateTime.now()
            );

            when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
            when(lineItemMapper.toEntity(lineRequest)).thenReturn(lineItem);
            when(lineItemRepository.save(any(InvoiceLineItem.class))).thenReturn(lineItem);
            when(lineItemMapper.toResponse(lineItem)).thenReturn(lineItemResponse);
            when(lineItemRepository.findByInvoiceId(invoiceId)).thenReturn(List.of(lineItem));
            when(invoiceRepository.save(any(Invoice.class))).thenReturn(invoice);

            LineItemResponse result = invoiceService.addLineItem(lineRequest);

            assertThat(result).isNotNull();
            verify(lineItemRepository).save(any(InvoiceLineItem.class));
        }
    }

    @Nested
    @DisplayName("createInvoiceFromMatchedRequest")
    class CreateInvoiceFromMatchedRequest {

        @Test
        @DisplayName("should create invoice from blood request matched event")
        void shouldCreateInvoiceFromBloodRequestMatchedEvent() {
            UUID requestId = UUID.randomUUID();
            UUID unitId1 = UUID.randomUUID();
            UUID unitId2 = UUID.randomUUID();
            BloodRequestMatchedEvent event = new BloodRequestMatchedEvent(
                    requestId, branchId, List.of(unitId1, unitId2), Instant.now()
            );

            when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> {
                Invoice saved = inv.getArgument(0);
                saved.setId(invoiceId);
                return saved;
            });
            when(lineItemRepository.save(any(InvoiceLineItem.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            invoiceService.createInvoiceFromMatchedRequest(event);

            ArgumentCaptor<Invoice> invoiceCaptor = ArgumentCaptor.forClass(Invoice.class);
            verify(invoiceRepository).save(invoiceCaptor.capture());

            Invoice savedInvoice = invoiceCaptor.getValue();
            assertThat(savedInvoice.getHospitalId()).isEqualTo(requestId);
            assertThat(savedInvoice.getBranchId()).isEqualTo(branchId);
            assertThat(savedInvoice.getInvoiceNumber()).startsWith("INV-");

            verify(lineItemRepository, times(2)).save(any(InvoiceLineItem.class));
        }
    }
}
